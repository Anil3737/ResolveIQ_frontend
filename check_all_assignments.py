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

def check_all_assignments():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            user_ids = [16, 24, 25] # Pushpa, Pratap, Gopi
            
            print("\n--- TEAMS LED BY THESE USERS ---")
            cursor.execute("SELECT id, name, department_id, team_lead_id FROM teams WHERE team_lead_id IN %s", (user_ids,))
            for row in cursor.fetchall():
                print(f"Team ID: {row['id']} | Name: {row['name']} | Dept ID: {row['department_id']} | Lead ID: {row['team_lead_id']}")
                
            print("\n--- TEAMS THESE USERS ARE MEMBERS OF ---")
            cursor.execute("""
                SELECT tm.team_id, tm.user_id, t.name, t.department_id 
                FROM team_members tm 
                JOIN teams t ON tm.team_id = t.id 
                WHERE tm.user_id IN %s
            """, (user_ids,))
            for row in cursor.fetchall():
                print(f"User ID: {row['user_id']} | Team ID: {row['team_id']} | Team: {row['name']} | Dept ID: {row['department_id']}")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_all_assignments()
