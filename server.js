const express = require('express');
const bodyParser = require('body-parser');
const session = require('express-session');
const bcrypt = require('bcryptjs');
const path = require('path');
const crypto = require('crypto');

// Simple UUID generator for CommonJS
const generateUUID = () => {
    return crypto.randomUUID();
};

const app = express();
const PORT = 3000;

// Middleware
app.set('view engine', 'ejs');
app.use(express.static(path.join(__dirname, 'public')));
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(session({
    secret: 'banking-app-secret-key-2024',
    resave: false,
    saveUninitialized: false,
    cookie: { secure: false, maxAge: 3600000 }
}));

// In-memory database (for demo purposes)
const db = {
    users: [],
    accounts: [],
    transactions: [],
    loans: [],
    cards: []
};

// Initialize with sample data
function initializeData() {
    // Create admin user
    const adminPassword = bcrypt.hashSync('admin123', 10);
    const userPassword = bcrypt.hashSync('user123', 10);
    
    const adminUser = {
        id: generateUUID(),
        username: 'admin',
        password: adminPassword,
        email: 'admin@bank.com',
        firstName: 'Admin',
        lastName: 'User',
        phone: '+1-555-0000',
        role: 'admin',
        createdAt: new Date()
    };
    
    const regularUser = {
        id: generateUUID(),
        username: 'john',
        password: userPassword,
        email: 'john@example.com',
        firstName: 'John',
        lastName: 'Doe',
        phone: '+1-555-0001',
        role: 'customer',
        createdAt: new Date()
    };
    
    db.users.push(adminUser, regularUser);
    
    // Create accounts
    const adminAccount = {
        id: generateUUID(),
        userId: adminUser.id,
        accountNumber: '1000000001',
        type: 'savings',
        balance: 50000.00,
        currency: 'USD',
        status: 'active',
        createdAt: new Date()
    };
    
    const userAccount1 = {
        id: generateUUID(),
        userId: regularUser.id,
        accountNumber: '1000000002',
        type: 'checking',
        balance: 10000.00,
        currency: 'USD',
        status: 'active',
        createdAt: new Date()
    };
    
    const userAccount2 = {
        id: generateUUID(),
        userId: regularUser.id,
        accountNumber: '1000000003',
        type: 'savings',
        balance: 25000.00,
        currency: 'USD',
        status: 'active',
        createdAt: new Date()
    };
    
    db.accounts.push(adminAccount, userAccount1, userAccount2);
    
    // Create sample transactions
    const transactions = [
        {
            id: generateUUID(),
            accountId: userAccount1.id,
            type: 'credit',
            amount: 5000.00,
            description: 'Salary Deposit',
            balance: 10000.00,
            timestamp: new Date(Date.now() - 86400000 * 5)
        },
        {
            id: generateUUID(),
            accountId: userAccount1.id,
            type: 'debit',
            amount: 200.00,
            description: 'ATM Withdrawal',
            balance: 9800.00,
            timestamp: new Date(Date.now() - 86400000 * 3)
        },
        {
            id: generateUUID(),
            accountId: userAccount1.id,
            type: 'debit',
            amount: 150.00,
            description: 'Online Purchase',
            balance: 9650.00,
            timestamp: new Date(Date.now() - 86400000 * 2)
        },
        {
            id: generateUUID(),
            accountId: userAccount1.id,
            type: 'credit',
            amount: 350.00,
            description: 'Transfer from Savings',
            balance: 10000.00,
            timestamp: new Date(Date.now() - 86400000)
        }
    ];
    
    db.transactions.push(...transactions);
    
    // Create sample card
    db.cards.push({
        id: generateUUID(),
        userId: regularUser.id,
        accountId: userAccount1.id,
        cardNumber: '4532-****-****-1234',
        cardType: 'debit',
        status: 'active',
        expiryDate: '12/27',
        cvv: '***'
    });
}

initializeData();

// Authentication middleware
function requireAuth(req, res, next) {
    if (!req.session.userId) {
        return res.redirect('/login');
    }
    next();
}

function requireAdmin(req, res, next) {
    if (!req.session.userId || req.session.userRole !== 'admin') {
        return res.redirect('/login');
    }
    next();
}

// Routes

// Home page
app.get('/', (req, res) => {
    if (req.session.userId) {
        return res.redirect('/dashboard');
    }
    res.render('index');
});

// Login
app.get('/login', (req, res) => {
    res.render('login', { error: null });
});

app.post('/login', (req, res) => {
    const { username, password } = req.body;
    const user = db.users.find(u => u.username === username);
    
    if (!user || !bcrypt.compareSync(password, user.password)) {
        return res.render('login', { error: 'Invalid username or password' });
    }
    
    req.session.userId = user.id;
    req.session.userRole = user.role;
    req.session.userName = user.firstName;
    res.redirect('/dashboard');
});

// Logout
app.get('/logout', (req, res) => {
    req.session.destroy();
    res.redirect('/');
});

// Register
app.get('/register', (req, res) => {
    res.render('register', { error: null, success: null });
});

app.post('/register', (req, res) => {
    const { username, password, confirmPassword, email, firstName, lastName, phone } = req.body;
    
    if (password !== confirmPassword) {
        return res.render('register', { error: 'Passwords do not match', success: null });
    }
    
    if (db.users.find(u => u.username === username)) {
        return res.render('register', { error: 'Username already exists', success: null });
    }
    
    const hashedPassword = bcrypt.hashSync(password, 10);
    const newUser = {
        id: generateUUID(),
        username,
        password: hashedPassword,
        email,
        firstName,
        lastName,
        phone,
        role: 'customer',
        createdAt: new Date()
    };
    
    db.users.push(newUser);
    
    // Create default account
    const newAccount = {
        id: generateUUID(),
        userId: newUser.id,
        accountNumber: `1000000${db.accounts.length + 1}`,
        type: 'checking',
        balance: 0.00,
        currency: 'USD',
        status: 'active',
        createdAt: new Date()
    };
    
    db.accounts.push(newAccount);
    
    res.render('register', { error: null, success: 'Registration successful! Please login.' });
});

// Dashboard
app.get('/dashboard', requireAuth, (req, res) => {
    const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
    const totalBalance = userAccounts.reduce((sum, acc) => sum + acc.balance, 0);
    
    // Get recent transactions across all accounts
    const accountIds = userAccounts.map(a => a.id);
    const recentTransactions = db.transactions
        .filter(t => accountIds.includes(t.accountId))
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 5);
    
    res.render('dashboard', {
        userName: req.session.userName,
        accounts: userAccounts,
        totalBalance,
        recentTransactions,
        role: req.session.userRole
    });
});

// Accounts
app.get('/accounts', requireAuth, (req, res) => {
    const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
    res.render('accounts', { accounts: userAccounts });
});

app.get('/accounts/:id', requireAuth, (req, res) => {
    const account = db.accounts.find(a => a.id === req.params.id && a.userId === req.session.userId);
    if (!account) {
        return res.redirect('/accounts');
    }
    
    const transactions = db.transactions
        .filter(t => t.accountId === account.id)
        .sort((a, b) => b.timestamp - a.timestamp);
    
    res.render('account-detail', { account, transactions });
});

// Transfer Money
app.get('/transfer', requireAuth, (req, res) => {
    const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
    res.render('transfer', { accounts: userAccounts, error: null, success: null });
});

app.post('/transfer', requireAuth, (req, res) => {
    const { fromAccount, toAccountNumber, amount, description } = req.body;
    const fromAcc = db.accounts.find(a => a.id === fromAccount && a.userId === req.session.userId);
    const toAcc = db.accounts.find(a => a.accountNumber === toAccountNumber);
    
    if (!fromAcc || !toAcc) {
        const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
        return res.render('transfer', { accounts: userAccounts, error: 'Invalid account number', success: null });
    }
    
    const transferAmount = parseFloat(amount);
    if (isNaN(transferAmount) || transferAmount <= 0) {
        const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
        return res.render('transfer', { accounts: userAccounts, error: 'Invalid amount', success: null });
    }
    
    if (fromAcc.balance < transferAmount) {
        const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
        return res.render('transfer', { accounts: userAccounts, error: 'Insufficient funds', success: null });
    }
    
    // Process transfer
    fromAcc.balance -= transferAmount;
    toAcc.balance += transferAmount;
    
    // Record transactions
    db.transactions.push({
        id: generateUUID(),
        accountId: fromAcc.id,
        type: 'debit',
        amount: transferAmount,
        description: description || `Transfer to ${toAcc.accountNumber}`,
        balance: fromAcc.balance,
        timestamp: new Date()
    });
    
    db.transactions.push({
        id: generateUUID(),
        accountId: toAcc.id,
        type: 'credit',
        amount: transferAmount,
        description: description || `Transfer from ${fromAcc.accountNumber}`,
        balance: toAcc.balance,
        timestamp: new Date()
    });
    
    const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
    res.render('transfer', { accounts: userAccounts, error: null, success: 'Transfer completed successfully!' });
});

// Transaction History
app.get('/transactions', requireAuth, (req, res) => {
    const userAccounts = db.accounts.filter(a => a.userId === req.session.userId);
    const accountIds = userAccounts.map(a => a.id);
    const transactions = db.transactions
        .filter(t => accountIds.includes(t.accountId))
        .sort((a, b) => b.timestamp - a.timestamp);
    
    res.render('transactions', { transactions, accounts: userAccounts });
});

// Cards
app.get('/cards', requireAuth, (req, res) => {
    const userCards = db.cards.filter(c => c.userId === req.session.userId);
    res.render('cards', { cards: userCards });
});

app.post('/cards/request', requireAuth, (req, res) => {
    const { accountId, cardType } = req.body;
    const account = db.accounts.find(a => a.id === accountId && a.userId === req.session.userId);
    
    if (!account) {
        return res.redirect('/cards');
    }
    
    const newCard = {
        id: generateUUID(),
        userId: req.session.userId,
        accountId: account.id,
        cardNumber: `4532-****-****-${Math.floor(1000 + Math.random() * 9000)}`,
        cardType: cardType || 'debit',
        status: 'pending',
        expiryDate: `${String(new Date().getMonth() + 1).padStart(2, '0')}/${String(new Date().getFullYear() + 5).slice(-2)}`,
        cvv: '***'
    };
    
    db.cards.push(newCard);
    res.redirect('/cards');
});

// Loans
app.get('/loans', requireAuth, (req, res) => {
    const userLoans = db.loans.filter(l => l.userId === req.session.userId);
    res.render('loans', { loans: userLoans });
});

app.post('/loans/apply', requireAuth, (req, res) => {
    const { loanType, amount, term } = req.body;
    
    const newLoan = {
        id: generateUUID(),
        userId: req.session.userId,
        loanType,
        amount: parseFloat(amount),
        term: parseInt(term),
        interestRate: loanType === 'personal' ? 8.5 : loanType === 'home' ? 6.5 : 12.0,
        status: 'pending',
        appliedAt: new Date()
    };
    
    db.loans.push(newLoan);
    res.redirect('/loans');
});

// Profile
app.get('/profile', requireAuth, (req, res) => {
    const user = db.users.find(u => u.id === req.session.userId);
    res.render('profile', { user, error: null, success: null });
});

app.post('/profile', requireAuth, (req, res) => {
    const { email, phone, currentPassword, newPassword } = req.body;
    const user = db.users.find(u => u.id === req.session.userId);
    
    if (newPassword) {
        if (!currentPassword || !bcrypt.compareSync(currentPassword, user.password)) {
            return res.render('profile', { user, error: 'Current password is incorrect', success: null });
        }
        user.password = bcrypt.hashSync(newPassword, 10);
    }
    
    user.email = email;
    user.phone = phone;
    
    res.render('profile', { user, error: null, success: 'Profile updated successfully!' });
});

// Admin Panel
app.get('/admin', requireAdmin, (req, res) => {
    const stats = {
        totalUsers: db.users.filter(u => u.role === 'customer').length,
        totalAccounts: db.accounts.length,
        totalDeposits: db.accounts.reduce((sum, acc) => sum + acc.balance, 0),
        pendingLoans: db.loans.filter(l => l.status === 'pending').length
    };
    
    const users = db.users.filter(u => u.role === 'customer');
    const loans = db.loans;
    
    res.render('admin', { stats, users, loans });
});

app.post('/admin/approve-loan/:id', requireAdmin, (req, res) => {
    const loan = db.loans.find(l => l.id === req.params.id);
    if (loan) {
        loan.status = 'approved';
    }
    res.redirect('/admin');
});

app.post('/admin/reject-loan/:id', requireAdmin, (req, res) => {
    const loan = db.loans.find(l => l.id === req.params.id);
    if (loan) {
        loan.status = 'rejected';
    }
    res.redirect('/admin');
});

// Start server
app.listen(PORT, () => {
    console.log(`Banking application running at http://localhost:${PORT}`);
});
