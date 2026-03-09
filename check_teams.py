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

def check_teams():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            print("\n--- TEAMS ---")
            cursor.execute("SELECT id, name, issue_type, department_id, team_lead_id FROM teams")
            teams = cursor.fetchall()
            for row in teams:
                print(f"ID: {row['id']} | Name: {row['name']} | Issue: {row['issue_type']} | Dept ID: {row['department_id']} | Lead ID: {row['team_lead_id']}")
                
            print("\n--- TEAM MEMBERS (Pratap, Gopi) ---")
            cursor.execute("""
                SELECT tm.team_id, tm.user_id, u.full_name 
                FROM team_members tm 
                JOIN users u ON tm.user_id = u.id 
                WHERE u.full_name LIKE '%Pratap%' OR u.full_name LIKE '%Gopi%'
            """)
            for row in cursor.fetchall():
                print(f"Team ID: {row['team_id']} | User: {row['full_name']} (ID: {row['user_id']})")
                
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    check_teams()
