import sys, os
# Add backend to path
sys.path.insert(0, r"c:\Users\DELL\OneDrive\Desktop\resolveiq_backend")
from app import create_app
from app.extensions import db
from sqlalchemy import text

app = create_app()
with app.app_context():
    print("--- FIXING SCHEMA ---")
    try:
        # Check if column exists first
        from sqlalchemy import inspect
        inspector = inspect(db.engine)
        columns = [c['name'] for c in inspector.get_columns('tickets')]
        
        if 'ai_explanation' not in columns:
            print("Adding ai_explanation column...")
            # Use TEXT for broad compatibility if JSON is tricky
            db.session.execute(text("ALTER TABLE tickets ADD COLUMN ai_explanation TEXT NULL AFTER escalation_required"))
            db.session.commit()
            print("✅ Column added successfully.")
        else:
            print("✅ Column already exists.")
            
    except Exception as e:
        print(f"❌ Error adding column: {e}")
    
    print("--- END ---")
