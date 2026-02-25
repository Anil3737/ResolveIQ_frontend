import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.models.user import User

app = create_app()
with app.app_context():
    users = User.query.all()
    with open("users_output.txt", "w") as f:
        f.write("--- USER LIST ---\n")
        for u in users:
            role_name = u.role.name if u.role else 'N/A'
            f.write(f"ID: {u.id} | Email: {u.email} | Role: {role_name} | Name: {u.full_name}\n")
        f.write("--- END ---\n")
    print("User list saved to users_output.txt")
