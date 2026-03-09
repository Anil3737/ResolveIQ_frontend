import sys
import os

# Add backend to sys.path
backend_path = r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend"
sys.path.append(backend_path)

from app import create_app
from app.services.ticket_service import TicketService
from app.models.ticket import Ticket
from app.extensions import db

def test_creation():
    app = create_app()
    with app.app_context():
        # Using a dummy user ID (e.g., 1)
        data = {
            "title": "TEST SOFTWARE TICKET",
            "description": "Test description",
            "issue_type": "Software Installation",
            "location": "TEST LOC"
        }
        
        print("\n--- Creating Software Installation Ticket ---")
        ticket = TicketService.create_ticket(data, user_id=1)
        print(f"Result: Ticket No {ticket.ticket_number} | Dept ID {ticket.department_id}")

        data2 = {
            "title": "TEST APP DOWNTIME TICKET",
            "description": "Test description",
            "issue_type": "Application Downtime / Application Issues",
            "location": "TEST LOC"
        }
        print("\n--- Creating Application Downtime Ticket ---")
        ticket2 = TicketService.create_ticket(data2, user_id=1)
        print(f"Result: Ticket No {ticket2.ticket_number} | Dept ID {ticket2.department_id}")

if __name__ == "__main__":
    test_creation()
