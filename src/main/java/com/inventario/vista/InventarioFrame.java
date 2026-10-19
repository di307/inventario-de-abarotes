package com.inventario.vista;

import com.inventario.dao.ProductoDAO;
import com.inventario.db.ConexionBD;
import com.inventario.modelo.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Ventana principal del sistema de inventario. */
public class InventarioFrame extends JFrame {

    private static final Color AZUL = new Color(0x1F6FEB);
    private static final Color VERDE = new Color(0x2DA44E);
    private static final Color NARANJA = new Color(0xE08A00);
    private static final Color ROJO = new Color(0xCF222E);
    private static final Color GRIS = new Color(0x6E7781);
    private static final int STOCK_BAJO = 5;

    private final ProductoDAO dao = new ProductoDAO();
    private boolean conectado = false;

    private final JTextField txtId = new JTextField();
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCategoria = new JTextField();
    private final JTextField txtPrecioCompra = new JTextField();
    private final JTextField txtPrecioVenta = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JTextField txtVencimiento = new JTextField();
    private final JTextField txtBuscar = new JTextField(22);

    private final JLabel lblEstado = new JLabel("Desconectado");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Código", "Nombre", "Categoría", "P. Compra", "P. Venta", "Stock", "Vence"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public InventarioFrame() {
        super("Sistema de Inventario - Alimentos para Mascotas y Abarrotes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.WEST);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        configurarTabla();
        txtId.setEditable(false);
    }

    // ------------------------------------------------------------------ UI

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Control de Inventario");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(new Color(0x24292F));

        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEstado.setForeground(ROJO);

        JButton btnConectar = crearBoton("Conectar a la BD", AZUL);
        btnConectar.addActionListener(e -> conectar());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.add(lblEstado);
        derecha.add(btnConectar);

        p.add(titulo, BorderLayout.WEST);
        p.add(derecha, BorderLayout.EAST);
        p.setBorder(new EmptyBorder(0, 0, 5, 0));
        return p;
    }

    private JPanel crearFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7DE)), "Datos del producto",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)));
        p.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx = 0;

        String[] etiquetas = {"ID", "Código *", "Nombre *", "Categoría *",
                "Precio compra *", "Precio venta *", "Stock *", "Vencimiento (AAAA-MM-DD)"};
        JTextField[] campos = {txtId, txtCodigo, txtNombre, txtCategoria,
                txtPrecioCompra, txtPrecioVenta, txtStock, txtVencimiento};

        int fila = 0;
        for (int i = 0; i < etiquetas.length; i++) {
            g.gridy = fila++;
            p.add(new JLabel(etiquetas[i]), g);
            g.gridy = fila++;
            campos[i].setPreferredSize(new Dimension(200, 30));
            p.add(campos[i], g);
        }
        g.gridy = fila;
        g.weighty = 1;
        p.add(Box.createVerticalGlue(), g);
        return p;
    }

    private JPanel crearPanelTabla() {
        JPanel p = new JPanel(new BorderLayout(0, 8));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        busqueda.add(new JLabel("Buscar (código, nombre o categoría):"));
        busqueda.add(txtBuscar);
        JButton btnBuscar = crearBoton("Buscar", GRIS);
        JButton btnTodos = crearBoton("Ver todo", GRIS);
        btnBuscar.addActionListener(e -> buscar());
        btnTodos.addActionListener(e -> { txtBuscar.setText(""); recargarTabla(); });
        txtBuscar.addActionListener(e -> buscar());
        busqueda.add(btnBuscar);
        busqueda.add(btnTodos);

        p.add(busqueda, BorderLayout.NORTH);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JLabel leyenda = new JLabel("Rojo: vencido o sin stock  |  Naranja: stock bajo (≤ " + STOCK_BAJO
                + ") o vence en 30 días");
        leyenda.setForeground(GRIS);
        p.add(leyenda, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));

        JButton btnGuardar = crearBoton("Guardar / Registrar", VERDE);
        JButton btnActualizar = crearBoton("Actualizar", AZUL);
        JButton btnEliminar = crearBoton("Eliminar", ROJO);
        JButton btnVender = crearBoton("Descontar Stock", NARANJA);
        JButton btnLimpiar = crearBoton("Limpiar Campos", GRIS);

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnVender.addActionListener(e -> descontarStock());
        btnLimpiar.addActionListener(e -> limpiar());

        p.add(btnGuardar);
        p.add(btnActualizar);
        p.add(btnEliminar);
        p.add(btnVender);
        p.add(btnLimpiar);
        return p;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 36));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void configurarTabla() {
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(240);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    int r = t.convertRowIndexToModel(row);
                    c.setForeground(colorFila(r));
                } else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaEnFormulario();
        });
    }

    private Color colorFila(int fila) {
        try {
            int stock = Integer.parseInt(modelo.getValueAt(fila, 6).toString());
            Object venc = modelo.getValueAt(fila, 7);
            LocalDate hoy = LocalDate.now();
            if (stock == 0) return ROJO;
            if (venc != null && !venc.toString().isEmpty()) {
                LocalDate f = LocalDate.parse(venc.toString());
                if (f.isBefore(hoy)) return ROJO;
                if (!f.isAfter(hoy.plusDays(30))) return NARANJA;
            }
            if (stock <= STOCK_BAJO) return NARANJA;
        } catch (RuntimeException ignored) { }
        return Color.BLACK;
    }

    // ------------------------------------------------------------ acciones

    private void conectar() {
        JTextField usuario = new JTextField("root");
        JPasswordField clave = new JPasswordField();
        Object[] campos = {"Usuario MySQL:", usuario, "Contraseña:", clave,
                "Base de datos: inventario_db (localhost:3306)"};
        int op = JOptionPane.showConfirmDialog(this, campos, "Conectar a la BD",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;

        ConexionBD.configurar(usuario.getText().trim(), new String(clave.getPassword()));
        try {
            ConexionBD.probarConexion();
            conectado = true;
            lblEstado.setText("Conectado");
            lblEstado.setForeground(VERDE);
            recargarTabla();
            JOptionPane.showMessageDialog(this, "Conexión exitosa a la base de datos.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            conectado = false;
            lblEstado.setText("Desconectado");
            lblEstado.setForeground(ROJO);
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos.\n\nDetalle: " + ex.getMessage()
                            + "\n\nVerifique que MySQL esté iniciado, que haya ejecutado schema.sql\n"
                            + "y que el usuario/contraseña sean correctos.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        if (!verificarConexion()) return;
        try {
            Producto p = leerFormulario(false);
            dao.insertar(p);
            recargarTabla();
            limpiar();
            info("Producto registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void actualizar() {
        if (!verificarConexion()) return;
        try {
            Producto p = leerFormulario(true);
            if (dao.actualizar(p)) {
                recargarTabla();
                limpiar();
                info("Producto actualizado correctamente.");
            } else {
                advertencia("No se encontró el producto a actualizar.");
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void eliminar() {
        if (!verificarConexion()) return;
        if (txtId.getText().isBlank()) {
            advertencia("Seleccione un producto de la tabla para eliminarlo.");
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto \"" + txtNombre.getText() + "\"? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) return;
        try {
            if (dao.eliminar(Integer.parseInt(txtId.getText()))) {
                recargarTabla();
                limpiar();
                info("Producto eliminado.");
            } else {
                advertencia("El producto ya no existe.");
            }
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void descontarStock() {
        if (!verificarConexion()) return;
        if (txtId.getText().isBlank()) {
            advertencia("Seleccione un producto de la tabla para descontar stock.");
            return;
        }
        String entrada = JOptionPane.showInputDialog(this,
                "Cantidad a descontar de \"" + txtNombre.getText() + "\":", "Descontar stock",
                JOptionPane.QUESTION_MESSAGE);
        if (entrada == null) return;
        try {
            int cantidad = Integer.parseInt(entrada.trim());
            if (cantidad <= 0) {
                advertencia("La cantidad debe ser mayor que cero.");
                return;
            }
            if (dao.descontarStock(Integer.parseInt(txtId.getText()), cantidad)) {
                recargarTabla();
                limpiar();
                info("Stock actualizado correctamente.");
            } else {
                advertencia("Stock insuficiente: la cantidad solicitada supera las existencias.");
            }
        } catch (NumberFormatException ex) {
            advertencia("Ingrese una cantidad entera válida.");
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void buscar() {
        if (!verificarConexion()) return;
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            recargarTabla();
            return;
        }
        try {
            llenarTabla(dao.buscar(texto));
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void recargarTabla() {
        try {
            llenarTabla(dao.listar());
        } catch (SQLException ex) {
            errorSql(ex);
        }
    }

    private void llenarTabla(List<Producto> lista) {
        modelo.setRowCount(0);
        for (Producto p : lista) {
            modelo.addRow(new Object[]{
                    p.getId(), p.getCodigo(), p.getNombre(), p.getCategoria(),
                    p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(),
                    p.getFechaVencimiento() == null ? "" : p.getFechaVencimiento().toString()});
        }
    }

    private void cargarFilaEnFormulario() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int f = tabla.convertRowIndexToModel(vista);
        txtId.setText(String.valueOf(modelo.getValueAt(f, 0)));
        txtCodigo.setText(String.valueOf(modelo.getValueAt(f, 1)));
        txtNombre.setText(String.valueOf(modelo.getValueAt(f, 2)));
        txtCategoria.setText(String.valueOf(modelo.getValueAt(f, 3)));
        txtPrecioCompra.setText(String.valueOf(modelo.getValueAt(f, 4)));
        txtPrecioVenta.setText(String.valueOf(modelo.getValueAt(f, 5)));
        txtStock.setText(String.valueOf(modelo.getValueAt(f, 6)));
        txtVencimiento.setText(String.valueOf(modelo.getValueAt(f, 7)));
    }

    private void limpiar() {
        for (JTextField t : new JTextField[]{txtId, txtCodigo, txtNombre, txtCategoria,
                txtPrecioCompra, txtPrecioVenta, txtStock, txtVencimiento}) {
            t.setText("");
        }
        tabla.clearSelection();
        txtCodigo.requestFocusInWindow();
    }

    // ---------------------------------------------------------- validación

    /** Lee y valida el formulario. Lanza IllegalArgumentException con un mensaje claro. */
    private Producto leerFormulario(boolean requiereId) {
        Producto p = new Producto();

        if (requiereId) {
            if (txtId.getText().isBlank()) {
                throw new IllegalArgumentException("Seleccione un producto de la tabla para actualizarlo.");
            }
            p.setId(Integer.parseInt(txtId.getText().trim()));
        }

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String categoria = txtCategoria.getText().trim();
        if (codigo.isEmpty() || nombre.isEmpty() || categoria.isEmpty()
                || txtPrecioCompra.getText().isBlank() || txtPrecioVenta.getText().isBlank()
                || txtStock.getText().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios (*).");
        }
        if (codigo.length() > 30) throw new IllegalArgumentException("El código no puede superar 30 caracteres.");
        if (nombre.length() > 120) throw new IllegalArgumentException("El nombre no puede superar 120 caracteres.");
        if (categoria.length() > 60) throw new IllegalArgumentException("La categoría no puede superar 60 caracteres.");

        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setCategoria(categoria);

        try {
            BigDecimal compra = new BigDecimal(txtPrecioCompra.getText().trim().replace(',', '.'));
            BigDecimal venta = new BigDecimal(txtPrecioVenta.getText().trim().replace(',', '.'));
            if (compra.signum() < 0 || venta.signum() < 0) {
                throw new IllegalArgumentException("Los precios no pueden ser negativos.");
            }
            p.setPrecioCompra(compra);
            p.setPrecioVenta(venta);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Los precios deben ser números válidos (ej. 12.50).");
        }

        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
            p.setStock(stock);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El stock debe ser un número entero.");
        }

        String venc = txtVencimiento.getText().trim();
        if (!venc.isEmpty()) {
            try {
                p.setFechaVencimiento(LocalDate.parse(venc));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("La fecha de vencimiento debe tener el formato AAAA-MM-DD.");
            }
        }
        return p;
    }

    // ------------------------------------------------------------- mensajes

    private boolean verificarConexion() {
        if (!conectado) {
            advertencia("Primero debe conectarse a la base de datos (botón \"Conectar a la BD\").");
        }
        return conectado;
    }

    private void errorSql(SQLException ex) {
        if (ex.getErrorCode() == 1062) {
            advertencia("Ya existe un producto con ese código.");
        } else {
            JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void advertencia(String m) {
        JOptionPane.showMessageDialog(this, m, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
}
