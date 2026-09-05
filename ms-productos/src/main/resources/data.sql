-- Datos iniciales de la tienda (perfil demo, base H2 en memoria).
-- La base se crea vacia en cada arranque, por lo que basta con insertar.
INSERT INTO productos (nombre, descripcion, precio, stock) VALUES
  ('Alimento Premium Adulto 15kg', 'Croquetas balanceadas para perro adulto', 32990, 25),
  ('Alimento Cachorro 10kg',       'Nutricion completa para cachorros',       27990, 18),
  ('Snacks Dentales',              'Snacks para la higiene dental canina',     4990, 60),
  ('Cama Ortopedica Mediana',      'Cama con espuma viscoelastica',           45990,  8),
  ('Juguete Mordedor Resistente',  'Juguete de caucho natural',                7990, 32);
