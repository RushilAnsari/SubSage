🏠 Home / Subscriptions screen

Top bar: SubSage – Smart Subscriptions & Bill Manager

Left side: big buttons for common services:

Netflix 🔴

Spotify 🎵

Du 📶

Etisalat 📶

DEWA 💡

+ Other ➕

Right side: a table of all subscriptions:

Name	Amount	Cycle	Next Due	Auto Renew	Status
Netflix	39.00	Monthly	05-Jan	Yes	Active
Du Home	299.00	Monthly	10-Jan	Yes	Active

Bottom: buttons: Add, Edit, Delete, Mark Paid, Export

What happens when they press Netflix (your example)

Button Netflix’s ActionListener runs:

It asks the SubSageManager:
“Do we already have a subscription with name = ‘Netflix’?”

Two cases:

Case A – Not yet saved

Show a small dialog/form:

Amount

Billing cycle (dropdown: Monthly/Yearly)

Due date (date picker or text)

Payment method

Auto-renew checkbox

User fills and clicks Save

GUI creates a Subscription object and calls
manager.addSubscription(subscription);

Table refreshes and shows the new row.

Case B – Already exists

Show a details popup or open an edit form with existing data:

They can change amount, due date, etc.

Clicking Save updates that subscription in the manager (and DB/file).