import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.extensions import db
from sqlalchemy import inspect

app = create_app()
with app.app_context():
    print("--- DB DIAGNOSIS ---")
    inspector = inspect(db.engine)
    columns = [c['name'] for c in inspector.get_columns('tickets')]
    print(f"Tickets columns: {columns}")
    
    if 'ai_explanation' not in columns:
        print("❌ MISSING COLUMN: ai_explanation")
    else:
        print("✅ Column ai_explanation exists.")
    
    # Check users
    try:
        from app.models.user import User
        admin = User.query.filter_by(email='admin@company.com').first()
        if admin:
            print(f"✅ User admin@company.com exists (ID: {admin.id}, Role: {admin.role.name})")
        else:
            print("❌ MISSING USER: admin@company.com")
            
        old_admin = User.query.filter_by(email='admin@resolveiq.com').first()
        if old_admin:
            print(f"✅ User admin@resolveiq.com exists (ID: {old_admin.id}, Role: {old_admin.role.name})")
            
    except Exception as e:
        print(f"❌ Error checking users: {e}")
    
    print("--- END ---")
