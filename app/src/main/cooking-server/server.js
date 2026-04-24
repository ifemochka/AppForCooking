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


async function start() {
    await initDatabase();

    //Подключение сервера


    app.listen(PORT, '0.0.0.0', () => {
        console.log(`http://localhost:${PORT}`);
        console.log(`http://${localIp}:${PORT}`);
    });
}

start().catch(console.error);