db = db.getSiblingDB('admin');
db.auth('root', 'secret');
db = db.getSiblingDB('shop');
db.createUser({
  user: 'user',
  pwd: 'secret',
  roles: [{ role: 'readWrite', db: 'shop' }]
});