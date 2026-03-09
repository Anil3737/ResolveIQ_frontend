import pymysql
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', '127.0.0.1'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'resolveiq'),
    'cursorclass': pymysql.cursors.DictCursor
}

def check_db():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            print("\n--- USERS (Pushpa, Pratap, Gopi) ---")
            cursor.execute("""
                SELECT u.id, u.full_name, u.email, r.name as role
                FROM users u 
                JOIN roles r ON u.role_id = r.id 
                WHERE u.full_name LIKE '%Pushpa%' 
                   OR u.full_name LIKE '%Pratap%' 
                   OR u.full_name LIKE '%Gopi%'
            """)
            users = cursor.fetchall()
            if not users:
                print("No users found matching Pushpa, Pratap, or Gopi.")
            else:
                for row in users:
                    print(f"ID: {row['id']} | Name: {row['full_name']} | Role: {row['role']}")
                    # Check profiles
                    if row['role'] == 'TEAM_LEAD':
                        cursor.execute("SELECT department_id FROM team_lead_profiles WHERE user_id = %s", (row['id'],))
                        profile = cursor.fetchone()
                        print(f"   -> TeamLeadProfile Dept ID: {profile['department_id'] if profile else 'MISSING'}")
                    elif row['role'] == 'AGENT':
                        cursor.execute("SELECT department_id FROM agent_profiles WHERE user_id = %s", (row['id'],))
                        profile = cursor.fetchone()
                        print(f"   -> AgentProfile Dept ID: {profile['department_id'] if profile else 'MISSING'}")
                    elif row['role'] == 'EMPLOYEE':
                        cursor.execute("SELECT department_id FROM employee_profiles WHERE user_id = %s", (row['id'],))
                        profile = cursor.fetchone()
                        print(f"   -> EmployeeProfile Dept ID: {profile['department_id'] if profile else 'MISSING'}")

            print("\n--- RECENT TICKETS (Last 10) ---")
            cursor.execute("""
                SELECT t.id, t.ticket_number, t.title, t.department_id, d.name as dept_name 
                FROM tickets t 
                LEFT JOIN departments d ON t.department_id = d.id 
                ORDER BY t.created_at DESC LIMIT 10
            """)
            for row in cursor.fetchall():
                print(f"ID: {row['id']} | Num: {row['ticket_number']} | Title: {row['title']} | Dept: {row['dept_name']} (ID: {row['department_id']})")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_db()
