const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3');
const { open } = require('sqlite');
const path = require('path');
const fs = require('fs');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');


const SECRET_KEY = '';

const app = express();
const PORT = 3000;

app.use(cors({ origin: '*' }));
app.use(express.json());

let productsList = [];
let recipesList = [];
let ingredientsList = [];

try {
    const productsData = JSON.parse(fs.readFileSync('./products.json', 'utf8'));
    productsList = Array.isArray(productsData) ? productsData : (productsData.products || []);
    console.log(`Загружен products.json (${productsList.length} продуктов)`);
} catch (err) {
    console.log('products.json не найден');
}

try {
    const recipesData = JSON.parse(fs.readFileSync('./recipes.json', 'utf8'));
    recipesList = Array.isArray(recipesData) ? recipesData : (recipesData.recipes || []);
    console.log(`Загружен recipes.json (${recipesList.length} рецептов)`);
} catch (err) {
    console.log('recipes.json не найден');
}

try {
    const ingredientsData = JSON.parse(fs.readFileSync('./recipe_ingredients.json', 'utf8'));
    ingredientsList = Array.isArray(ingredientsData) ? ingredientsData : (ingredientsData.recipe_ingredients || []);
    console.log(`Загружен recipe_ingredients.json (${ingredientsList.length} ингредиентов)`);
} catch (err) {
    console.log('recipe_ingredients.json не найден');
}

let db;

async function initDatabase() {
    db = await open({
            filename: path.join(__dirname, 'cooking.db'),
            driver: sqlite3.Database
        });

    await db.exec(`
        CREATE TABLE IF NOT EXISTS product (
            product_id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            category TEXT NOT NULL,
            default_unit TEXT NOT NULL,
            calories_per_100g INTEGER NOT NULL,
            barcode TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS recipe (
            recipe_id INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT,
            cooking_time_minutes INTEGER,
            difficulty TEXT,
            image_url TEXT,
            calories_total INTEGER,
            instructions TEXT
        );

        CREATE TABLE IF NOT EXISTS recipe_ingredient (
            recipe_ingredient_id INTEGER PRIMARY KEY,
            recipe_id INTEGER NOT NULL,
            product_id INTEGER NOT NULL,
            quantity REAL NOT NULL,
            unit TEXT NOT NULL,
            UNIQUE(recipe_id, product_id),
            FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
            FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at INTEGER NOT NULL
            );

            CREATE TABLE IF NOT EXISTS user_profile (
                profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL UNIQUE,
                first_name TEXT,
                last_name TEXT,
                birth_date TEXT,
                avatar_url TEXT,
                FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS pantry_item (
                pantry_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                is_low INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
                UNIQUE(user_id, product_id)
            );

            CREATE TABLE IF NOT EXISTS allergy (
                allergy_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
                UNIQUE(user_id, product_id)
            );

            CREATE TABLE IF NOT EXISTS cooking_history (
                history_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                recipe_id INTEGER NOT NULL,
                cooked_at INTEGER NOT NULL,
                rating INTEGER,
                FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE
            );
    `);

    if (productsList.length > 0) {
        await db.run('DELETE FROM product');
        for (const product of productsList) {
            await db.run(
                `INSERT OR REPLACE INTO product (product_id, name, category, default_unit, calories_per_100g, barcode)
                 VALUES (?, ?, ?, ?, ?, ?)`,
                [product.product_id, product.name, product.category, product.default_unit,
                 product.calories_per_100g, product.barcode]
            );
        }
        console.log(`Загружено ${productsList.length} продуктов`);
    }

    if (recipesList.length > 0) {
        await db.run('DELETE FROM recipe');
        for (const recipe of recipesList) {
            await db.run(
                `INSERT OR REPLACE INTO recipe (recipe_id, title, description, cooking_time_minutes, difficulty, image_url, calories_total, instructions)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
                [recipe.recipe_id, recipe.title, recipe.description, recipe.cooking_time_minutes,
                 recipe.difficulty, recipe.image_url, recipe.calories_total, recipe.instructions]
            );
        }
        console.log(`Загружено ${recipesList.length} рецептов`);
    }

    if (ingredientsList.length > 0) {
        await db.run('DELETE FROM recipe_ingredient');
        for (const ing of ingredientsList) {
            await db.run(
                `INSERT OR REPLACE INTO recipe_ingredient (recipe_ingredient_id, recipe_id, product_id, quantity, unit)
                 VALUES (?, ?, ?, ?, ?)`,
                [ing.recipe_ingredient_id, ing.recipe_id, ing.product_id, ing.quantity, ing.unit]
            );
        }
        console.log(`Загружено ${ingredientsList.length} ингредиентов`);
    }
}
// Все продукты
app.get('/api/products', async (req, res) => {
    try {
        const products = await db.all('SELECT * FROM product ORDER BY name');
        res.json(products);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Поиск продуктов
app.get('/api/products/search/:query', async (req, res) => {
    try {
        const query = req.params.query;
        const products = await db.all(
            'SELECT * FROM product WHERE name LIKE ? ORDER BY name LIMIT 20',
            [`%${query}%`]
        );
        res.json(products);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Все рецепты
app.get('/api/recipes', async (req, res) => {
    try {
        const recipes = await db.all('SELECT * FROM recipe ORDER BY title');
        res.json(recipes);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Все ингредиенты рецептов
app.get('/api/recipeIngredients', async (req, res) => {
    try {
        const recipe_ingredients = await db.all('SELECT * FROM recipe_ingredient ORDER BY recipe_id');
        res.json(recipe_ingredients);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Рецепт по ID
app.get('/api/recipes/:id', async (req, res) => {
    try {
        const recipe = await db.get('SELECT * FROM recipe WHERE recipe_id = ?', [req.params.id]);
        if (!recipe) return res.status(404).json({ error: 'Рецепт не найден' });

        const ingredients = await db.all(`
            SELECT ri.*, p.name as product_name, p.category, p.default_unit, p.calories_per_100g
            FROM recipe_ingredient ri
            JOIN product p ON ri.product_id = p.product_id
            WHERE ri.recipe_id = ?
        `, [req.params.id]);

        res.json({ ...recipe, ingredients });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/auth/register', async (req, res) => {
    try {
        const { email, password, firstName, lastName } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Укажите Email и пароль' });
        }

        const existingUser = await db.get('SELECT * FROM user WHERE email = ?', [email]);
        if (existingUser) {
            return res.status(400).json({ error: 'Пользователь уже существует' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const result = await db.run(
            'INSERT INTO user (email, password_hash, created_at) VALUES (?, ?, ?)',
            [email, hashedPassword, Date.now()]
        );
        const userId = result.lastID;

        await db.run(
            'INSERT INTO user_profile (user_id, first_name, last_name) VALUES (?, ?, ?)',
            [userId, firstName || '', lastName || '']
        );

        const token = jwt.sign({ userId, email }, SECRET_KEY, { expiresIn: '30d' });

        res.json({
            success: true,
            token,
            userId,
            email,
            firstName: firstName || '',
            lastName: lastName || ''
        });
    } catch (error) {
        res.status(500).json({ error: 'Ошибка сервера' });
    }
});

app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        console.log('Login attempt:', { email });

        if (!email || !password) {
            return res.status(400).json({ error: 'Укажите Email и пароль' });
        }

        const user = await db.get('SELECT * FROM user WHERE email = ?', [email]);

        if (!user) {
            console.log('User not found:', email);
            return res.status(401).json({ error: 'Пользователь не найден' });
        }

        const isValidPassword = await bcrypt.compare(password, user.password_hash);

        if (!isValidPassword) {
            console.log('Invalid password for:', email);
            return res.status(401).json({ error: 'Неверный пароль' });
        }

        const profile = await db.get('SELECT first_name, last_name FROM user_profile WHERE user_id = ?', [user.user_id]);

        const token = jwt.sign({ userId: user.user_id, email }, SECRET_KEY, { expiresIn: '30d' });

        console.log('Login successful:', { userId: user.user_id, email });

        res.json({
            success: true,
            token,
            userId: user.user_id,
            email: user.email,
            firstName: profile?.first_name || '',
            lastName: profile?.last_name || ''
        });

    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Ошибка сервера' });
    }
});

app.get('/api/user/sync', async (req, res) => {
    try {
        const userId = parseInt(req.query.userId);

        console.log('Sync request for userId:', userId);

        if (!userId) {
            return res.status(400).json({ error: 'userId required' });
        }

        const user = await db.get(`
            SELECT u.user_id, u.email, up.first_name, up.last_name,
                   up.avatar_url, up.birth_date
            FROM user u
            LEFT JOIN user_profile up ON u.user_id = up.user_id
            WHERE u.user_id = ?
        `, [userId]);

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        const pantryItems = await db.all(`
            SELECT pi.pantry_item_id, pi.product_id, p.name as product_name, pi.is_low
            FROM pantry_item pi
            JOIN product p ON pi.product_id = p.product_id
            WHERE pi.user_id = ?
        `, [userId]);

        const allergies = await db.all(`
            SELECT a.allergy_id, a.product_id, p.name as product_name
            FROM allergy a
            JOIN product p ON a.product_id = p.product_id
            WHERE a.user_id = ?
        `, [userId]);

        let shoppingList = [];
        try {
            shoppingList = await db.all(`
                SELECT sli.item_id, sli.product_id, p.name as product_name, sli.is_purchased
                FROM shopping_list_item sli
                JOIN product p ON sli.product_id = p.product_id
                WHERE sli.user_id = ?
            `, [userId]);
        } catch (err) {
            shoppingList = [];
        }

        let cookingHistory = [];
        try {
            cookingHistory = await db.all(`
                SELECT ch.history_id, ch.recipe_id, r.title as recipe_title,
                       ch.cooked_at, ch.rating
                FROM cooking_history ch
                JOIN recipe r ON ch.recipe_id = r.recipe_id
                WHERE ch.user_id = ?
                ORDER BY ch.cooked_at DESC
            `, [userId]);
        } catch (err) {
            cookingHistory = [];
        }
        const response = {
            success: true,
            userProfile: {
                userId: user.user_id,
                email: user.email,
                firstName: user.first_name || '',
                lastName: user.last_name || '',
                avatarUrl: user.avatar_url || null,
                birthDate: user.birth_date || null
            },
            pantryItems: pantryItems,
            allergies: allergies,
            shoppingList: shoppingList,
            cookingHistory: cookingHistory
        };

        res.json(response);

    } catch (error) {
        res.status(500).json({
            error: error.message,
            details: error.stack
        });
    }
});

app.get('/api/debug/check-pantry', async (req, res) => {
    try {
        const pantryItems = await db.all('SELECT * FROM pantry_item WHERE user_id = 1');
        res.json({
            pantryItems: pantryItems,
            message: 'Check console for details'
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.get('/api/debug/pantry-items', async (req, res) => {
    try {
        const pantryItems = await db.all(`
            SELECT
                pi.pantry_item_id,
                pi.user_id,
                pi.product_id,
                pi.is_low,
                p.name as product_name,
                u.email as user_email
            FROM pantry_item pi
            LEFT JOIN product p ON pi.product_id = p.product_id
            LEFT JOIN user u ON pi.user_id = u.user_id
            ORDER BY pi.user_id, pi.product_id
        `);

        res.json({
            success: true,
            count: pantryItems.length,
            items: pantryItems
        });
    } catch (error) {
        console.error('Ошибка:', error);
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/user/pantry/add', async (req, res) => {
    try {
        const { userId, productId, isLow } = req.body;

        if (!userId || !productId) {
            return res.status(400).json({ error: 'userId and productId required' });
        }

        const product = await db.get('SELECT * FROM product WHERE product_id = ?', [productId]);
        if (!product) {
            console.log('Product not found:', productId);
            return res.status(404).json({ error: 'Product not found' });
        }

        const existing = await db.get(
            'SELECT * FROM pantry_item WHERE user_id = ? AND product_id = ?',
            [userId, productId]
        );

        if (existing) {
            await db.run(
                'UPDATE pantry_item SET is_low = ? WHERE user_id = ? AND product_id = ?',
                [isLow ? 1 : 0, userId, productId]
            );
            res.json({
                success: true,
                message: 'Product updated in pantry',
                userId: userId,
                productId: productId
            });
        } else {
            await db.run(
                'INSERT INTO pantry_item (user_id, product_id, is_low) VALUES (?, ?, ?)',
                [userId, productId, isLow ? 1 : 0]
            );
            res.json({
                success: true,
                message: 'Product added to pantry',
                userId: userId,
                productId: productId
            });
        }

    } catch (error) {
        console.error('Add to pantry error:', error);
        res.status(500).json({ error: error.message });
    }
});

app.delete('/api/user/pantry/remove', async (req, res) => {
    try {
        const userId = parseInt(req.query.userId);
        const productId = parseInt(req.query.productId);

        if (!userId || !productId) {
            return res.status(400).json({ error: 'userId and productId required' });
        }

        const result = await db.run(
            'DELETE FROM pantry_item WHERE user_id = ? AND product_id = ?',
            [userId, productId]
        );

        if (result.changes > 0) {
            res.json({
                success: true,
                message: 'Product removed from pantry',
                deleted: result.changes
            });
        } else {
            res.json({
                success: true,
                message: 'No record found to delete',
                deleted: 0
            });
        }

    } catch (error) {
        console.error('Remove from pantry error:', error);
        res.status(500).json({ error: error.message });
    }
});

app.get('/api/user/pantry/:userId', async (req, res) => {
    try {
        const userId = parseInt(req.params.userId);

        const pantryItems = await db.all(`
            SELECT pi.pantry_item_id, pi.product_id, p.name as product_name, pi.is_low
            FROM pantry_item pi
            JOIN product p ON pi.product_id = p.product_id
            WHERE pi.user_id = ?
        `, [userId]);

        res.json({
            success: true,
            userId: userId,
            items: pantryItems,
            count: pantryItems.length
        });

    } catch (error) {
        console.error('Get pantry error:', error);
        res.status(500).json({ error: error.message });
    }
});

async function start() {
    await initDatabase();

    //Подключение сервера


    app.listen(PORT, '0.0.0.0', () => {
        console.log(`http://localhost:${PORT}`);
        console.log(`http://${localIp}:${PORT}`);
    });
}

start().catch(console.error);