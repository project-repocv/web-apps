# SecureBank - Modern Banking Web Application

A full-featured banking web application built with Node.js, Express, and EJS that demonstrates standard features found in modern banking applications.

## Features

### Customer Features
- **User Authentication**: Secure login/registration with password hashing (bcrypt)
- **Dashboard**: Overview of total balance, accounts, and recent transactions
- **Account Management**: View multiple accounts (checking, savings) with balances
- **Money Transfer**: Instant transfers between accounts within the bank
- **Transaction History**: Complete transaction history with filtering
- **Card Management**: View existing cards and request new debit/credit cards
- **Loan Applications**: Apply for personal, home, or auto loans
- **Profile Management**: Update contact information and change password

### Admin Features
- **Admin Dashboard**: Statistics on customers, accounts, and deposits
- **Loan Management**: Approve or reject loan applications
- **Customer Overview**: View all registered customers

## Technology Stack

- **Backend**: Node.js with Express.js
- **Frontend**: EJS templates with responsive CSS
- **Security**: bcryptjs for password hashing, express-session for session management
- **Data Storage**: In-memory database (for demo purposes)

## Installation & Setup

### Prerequisites
- Node.js (v14 or higher recommended)
- npm (comes with Node.js)

### Step 1: Navigate to the project directory
```bash
cd /workspace
```

### Step 2: Install dependencies (already done)
```bash
npm install
```

### Step 3: Start the application
```bash
npm start
```

Or alternatively:
```bash
node server.js
```

### Step 4: Access the application
Open your web browser and navigate to:
```
http://localhost:3000
```

## Demo Credentials

### Customer Account
- **Username**: `john`
- **Password**: `user123`
- **Features**: Full customer access with checking and savings accounts

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Features**: Admin panel access to manage loans and view statistics

## Testing Guide

### 1. Test User Registration
1. Go to http://localhost:3000
2. Click "Register" or navigate to http://localhost:3000/register
3. Fill in the registration form with your details
4. Submit and then login with your new credentials

### 2. Test Login
1. Navigate to http://localhost:3000/login
2. Enter username: `john` and password: `user123`
3. You should be redirected to the dashboard

### 3. Test Dashboard Features
- View your total balance across all accounts
- See your accounts listed with balances
- View recent transactions
- Use quick action buttons

### 4. Test Money Transfer
1. Navigate to the Transfer page
2. Select an account to transfer from
3. Enter recipient account number (try `1000000001` or `1000000003`)
4. Enter amount (e.g., `100`)
5. Add optional description
6. Click "Transfer Money"
7. Verify the transaction appears in your transaction history

### 5. Test Transaction History
1. Navigate to Transactions page
2. View all your transactions with dates, descriptions, and amounts
3. Credits show in green, debits in red

### 6. Test Card Management
1. Navigate to Cards page
2. View your existing debit card
3. Request a new card by filling out the form

### 7. Test Loan Application
1. Navigate to Loans page
2. Select loan type (Personal, Home, or Auto)
3. Enter amount and term
4. Submit application
5. View your pending loan application

### 8. Test Profile Management
1. Navigate to Profile page
2. Update your email or phone number
3. Optionally change your password
4. Save changes

### 9. Test Admin Panel
1. Logout from customer account
2. Login with admin credentials (`admin` / `admin123`)
3. Navigate to Admin panel
4. View statistics
5. If there are pending loans, approve or reject them
6. View all customers

## Account Numbers for Testing

When testing transfers, you can use these account numbers:
- `1000000001` - Admin's savings account ($50,000 balance)
- `1000000002` - John's checking account ($10,000 balance)
- `1000000003` - John's savings account ($25,000 balance)

## Pages Overview

| Page | URL | Description |
|------|-----|-------------|
| Home | `/` | Landing page with features overview |
| Login | `/login` | User authentication |
| Register | `/register` | New user registration |
| Dashboard | `/dashboard` | Main user dashboard |
| Accounts | `/accounts` | List of user accounts |
| Account Detail | `/accounts/:id` | Individual account with transactions |
| Transfer | `/transfer` | Money transfer form |
| Transactions | `/transactions` | Full transaction history |
| Cards | `/cards` | Card management |
| Loans | `/loans` | Loan applications |
| Profile | `/profile` | User profile settings |
| Admin | `/admin` | Admin panel (admin only) |

## Security Notes

⚠️ **This is a demo application**. For production use, you should:
- Replace in-memory storage with a real database (PostgreSQL, MongoDB, etc.)
- Add HTTPS/TLS encryption
- Implement rate limiting
- Add CSRF protection
- Implement proper input validation and sanitization
- Add two-factor authentication
- Implement account lockout after failed login attempts
- Add audit logging
- Comply with banking regulations (PCI-DSS, GDPR, etc.)

## License

ISC License - Free for educational and demonstration purposes.

---

**Enjoy testing SecureBank!** 🏦
