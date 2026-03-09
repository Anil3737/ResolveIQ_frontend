import sys
import os

# Add backend to sys.path
backend_path = r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend"
sys.path.append(backend_path)

from app import create_app
from app.models.user import User
from app.models.ticket import Ticket
from app.models.role import Role
from app.extensions import db

def simulate_pushpa_fetch():
    app = create_app()
    with app.app_context():
        # Find Srinu
        srinu = User.query.filter(User.full_name.like('%Srinu%')).first()
        if not srinu:
            print("--- Srinu not found! ---")
            return
            
        print(f"--- Found Srinu: ID {srinu.id} ---")
        
        # Check his profile
        profile = srinu.team_lead_profile
        if not profile:
            print("--- Pushpa has no TeamLeadProfile! ---")
            return
            
        print(f"--- Profile Dept ID: {profile.department_id} ({profile.department.name}) ---")
        
        # Simulating logic from ticket_routes.py
        dept_id = profile.department_id
        # Note: the actual route might use a different filter, let's check it closely
        query = Ticket.query.filter(Ticket.department_id == dept_id).order_by(Ticket.created_at.desc())
        
        tickets = query.all()
        print(f"\nFound {len(tickets)} tickets for Dept {dept_id}:")
        for t in tickets:
            print(f"   - [{t.ticket_number}] {t.title} (Dept ID: {t.department_id})")

        # Check if any Dept 4 tickets are in the result
        dept_4_tickets = [t for t in tickets if t.department_id == 4]
        if dept_4_tickets:
            print(f"\nMISMATCH! Found {len(dept_4_tickets)} Dept 4 tickets in Dept 3 results!")
        else:
            print("\nOK: No Dept 4 tickets found in Dept 3 results.")

if __name__ == "__main__":
    simulate_pushpa_fetch()
