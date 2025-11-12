# How to Add Questions to MongoDB Database

This guide explains **4 different methods** to add quiz questions to your MongoDB database in the future.

---

## 📊 Question Format

All questions must follow this structure:

```json
{
  "questionText": "Your question here?",
  "options": ["Option A", "Option B", "Option C", "Option D"],
  "correctOption": "Option B",
  "timeLimitSeconds": 15
}
```

**Important Notes:**
- `questionText`: The quiz question (required)
- `options`: Array of 4 answer choices (required)
- `correctOption`: Must exactly match one of the options (required)
- `timeLimitSeconds`: Time limit in seconds (default: 15)
- MongoDB will auto-generate the `_id` field

---

## Method 1: MongoDB Atlas Web Interface ⭐ (Easiest)

**Best for:** Quick manual additions, one-time setup

### Steps:

1. **Login to MongoDB Atlas**
   - Visit: https://cloud.mongodb.com
   - Use your MongoDB credentials

2. **Navigate to Collections**
   - Click **"Database"** in left sidebar
   - Click **"Browse Collections"**
   - Select database: `Network2`
   - Select collection: `questions`

3. **Add Question**
   - Click **"INSERT DOCUMENT"** button
   - Toggle to **"{ } JSON view"** (top right)
   - Paste your question:

   ```json
   {
     "questionText": "What is 2 + 2?",
     "options": ["2", "3", "4", "5"],
     "correctOption": "4",
     "timeLimitSeconds": 10
   }
   ```

4. **Click "Insert"** - Done! ✅

---

## Method 2: Java QuestionSeeder Utility ⭐ (Programmatic)

**Best for:** Bulk adding multiple questions via code

### Steps:

1. **Open the seeder file:**
   ```
   src/main/java/utils/QuestionSeeder.java
   ```

2. **Edit the code to add your questions:**

   ```java
   Question newQuestion = new Question();
   newQuestion.setQuestionText("What is the capital of Japan?");
   newQuestion.setOptions(new String[]{"Tokyo", "Kyoto", "Osaka", "Nagoya"});
   newQuestion.setCorrectOption("Tokyo");
   newQuestion.setTimeLimitSeconds(new Question.TimeLimit(20));
   
   qm.addQuestion(newQuestion);
   ```

3. **Run the seeder:**

   ```powershell
   cd "c:\Users\Sashini\NPAssignment\Backend\Network_Backend"
   mvn exec:java -Dexec.mainClass="utils.QuestionSeeder"
   ```

4. **Verify output:**
   - You'll see: "Question added successfully!"
   - Total count will be displayed

### Example: Add Multiple Questions at Once

```java
public static void main(String[] args) {
    QuestionManager qm = new QuestionManager();
    
    // Question 1
    Question q1 = new Question();
    q1.setQuestionText("What is the largest ocean?");
    q1.setOptions(new String[]{"Atlantic", "Pacific", "Indian", "Arctic"});
    q1.setCorrectOption("Pacific");
    q1.setTimeLimitSeconds(new Question.TimeLimit(15));
    qm.addQuestion(q1);
    
    // Question 2
    Question q2 = new Question();
    q2.setQuestionText("Who painted the Mona Lisa?");
    q2.setOptions(new String[]{"Van Gogh", "Da Vinci", "Picasso", "Michelangelo"});
    q2.setCorrectOption("Da Vinci");
    q2.setTimeLimitSeconds(new Question.TimeLimit(20));
    qm.addQuestion(q2);
    
    System.out.println("All questions added! Total: " + qm.getAllQuestions().size());
    qm.close();
}
```

---

## Method 3: MongoDB Compass (Desktop App) ⭐

**Best for:** Visual database management, easy editing

### Steps:

1. **Download MongoDB Compass**
   - Visit: https://www.mongodb.com/products/compass
   - Install for Windows

2. **Connect to Database**
   - Open MongoDB Compass
   - Click **"New Connection"**
   - Paste URI: 
     ```
     mongodb+srv://network2:Network%402@network2.iqmxbdi.mongodb.net/
     ```
   - Click **"Connect"**

3. **Navigate to Collection**
   - Expand `Network2` database
   - Click `questions` collection

4. **Insert Document**
   - Click **"ADD DATA"** dropdown
   - Select **"Insert Document"**
   - Use JSON view or form view
   - Add your question data
   - Click **"Insert"**

**Bonus:** Compass lets you:
- ✅ Edit existing questions visually
- ✅ Delete questions easily
- ✅ Search and filter questions
- ✅ See all data in table format

---

## Method 4: Admin Panel Frontend (Upcoming)

**Best for:** Non-technical users, production use

### Steps:

1. **Start the backend server:**
   ```powershell
   cd "c:\Users\Sashini\NPAssignment\Backend\Network_Backend"
   mvn exec:java -Dexec.mainClass="Main"
   ```

2. **Open Admin Dashboard** (in browser):
   ```
   http://localhost:8080/admin/index.html
   ```

3. **Login:**
   - Username: `admin`
   - Password: `admin123`

4. **Add Question:**
   - Fill out the "Add Question" form
   - Enter question text
   - Add 4 options
   - Select correct answer
   - Set time limit
   - Click **"Add Question"**

5. **The question is automatically saved to MongoDB!**

---

## 🔍 Verify Questions Were Added

### Option A: Check MongoDB Atlas
1. Go to Collections browser
2. Count documents in `questions` collection
3. View the newest entries

### Option B: Run Server and Check Console
```powershell
mvn exec:java -Dexec.mainClass="Main"
```

Look for output:
```
Connected to MongoDB database: Network2
Question Manager initialized with XX questions.
```

The number `XX` shows your total question count.

### Option C: Query via Code
Create a simple test file to list all questions:

```java
QuestionManager qm = new QuestionManager();
List<Question> all = qm.getAllQuestions();
System.out.println("Total questions: " + all.size());
for (Question q : all) {
    System.out.println("- " + q.getQuestionText());
}
qm.close();
```

---

## 📝 Common Issues & Solutions

### Issue 1: "correctOption doesn't match any option"
**Solution:** Ensure `correctOption` is spelled EXACTLY like one of the options (case-sensitive)

```json
// ❌ WRONG
"options": ["Paris", "London", "Berlin"],
"correctOption": "paris"  // lowercase!

// ✅ CORRECT
"options": ["Paris", "London", "Berlin"],
"correctOption": "Paris"
```

### Issue 2: "Connection refused" when running seeder
**Solution:** Check your `.env` file:
```bash
# File: .idea/.env
MONGO_URI=mongodb+srv://network2:Network%402@network2.iqmxbdi.mongodb.net/?appName=Network2
MONGO_DB=Network2
```

### Issue 3: Questions not appearing
**Solution:** 
1. Verify you're connected to the right database (`Network2`)
2. Check the collection name is `questions` (lowercase)
3. Restart the server to reload questions

---

## 🎯 Quick Reference

| Method | Difficulty | Speed | Best For |
|--------|-----------|-------|----------|
| MongoDB Atlas | Easy | Fast | Quick single additions |
| QuestionSeeder | Medium | Very Fast | Bulk imports, automation |
| MongoDB Compass | Easy | Fast | Visual management |
| Admin Panel | Very Easy | Fast | End users, production |

---

## 📚 Additional Resources

- **MongoDB Documentation:** https://docs.mongodb.com/
- **MongoDB Atlas Tutorial:** https://docs.atlas.mongodb.com/
- **Your Database:** https://cloud.mongodb.com/

---

## 💡 Pro Tips

1. **Backup before bulk operations:**
   - Export collection in MongoDB Atlas before major changes
   - Use Compass to export JSON files

2. **Use Question Seeder for development:**
   - Keep a file with test questions
   - Easy to reset database during testing

3. **Use Admin Panel for production:**
   - Let non-technical team members add questions
   - Secure with strong passwords

4. **Version control your questions:**
   - Keep a `sample-questions.json` file in your repo
   - Document question sources

---

**Need Help?** Check the MongoDB connection in `.idea/.env` and ensure your server is running!
