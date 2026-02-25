import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.models.user import User

app = create_app()
with app.app_context():
    users = User.query.all()
    with open("actual_users.txt", "w") as f:
        f.write(f"{'EMAIL':<30} | {'ROLE':<10} | {'FULL NAME':<20}\n")
        f.write("-" * 65 + "\n")
        for u in users:
            role_name = u.role.name if u.role else 'N/A'
            f.write(f"{u.email:<30} | {role_name:<10} | {u.full_name:<20}\n")
print("User list saved to actual_users.txt")
