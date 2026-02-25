import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.extensions import db
from app.models.user import User

app = create_app()
with app.app_context():
    admin_emails = ["admin@company.com", "admin@resolveiq.com"]
    new_password = "admin123"
    
    print("--- UPDATING ADMIN PASSWORDS ---")
    for email in admin_emails:
        user = User.query.filter_by(email=email).first()
        if user:
            user.set_password(new_password)
            db.session.commit()
            print(f"✅ Updated password for: {email}")
        else:
            print(f"❌ User not found: {email}")
    print("--- END ---")
