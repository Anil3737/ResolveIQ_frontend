import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.extensions import db
from app.models.user import User, TeamLeadProfile

app = create_app()
with app.app_context():
    # 1. Reset Admin password
    admin = User.query.filter_by(email='admin@resolveiq.com').first()
    if admin:
        admin.set_password('password')
        print(f"✅ Reset password for {admin.email} to 'password'")
    
    # 2. Reset Team Lead password
    lead = User.query.filter_by(email='anil@resolveiq.com').first()
    if lead:
        lead.set_password('password')
        print(f"✅ Reset password for {lead.email} to 'password'")
        
    # 3. Create admin@company.com if missing
    comp_admin = User.query.filter_by(email='admin@company.com').first()
    if not comp_admin:
        from app.models.role import Role
        admin_role = Role.query.filter_by(name='ADMIN').first()
        if admin_role:
            new_admin = User(
                full_name="Company Admin",
                email="admin@company.com",
                phone="ADMIN001",
                role_id=admin_role.id,
                is_active=True
            )
            new_admin.set_password('password')
            db.session.add(new_admin)
            print(f"✅ Created new admin: admin@company.com / password")
            
    db.session.commit()
    print("All credential fixes applied!")
