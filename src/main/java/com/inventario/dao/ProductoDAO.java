package com.inventario.dao;

import com.inventario.db.ConexionBD;
import com.inventario.modelo.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de operaciones (CRUD) sobre la tabla productos.
 * Todas las consultas usan PreparedStatement.
 */
public class ProductoDAO {

    private static final String COLUMNAS =
            "id, codigo_producto, nombre, categoria, precio_compra, precio_venta, stock, fecha_vencimiento";

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo_producto, nombre, categoria, "
                + "precio_compra, precio_venta, stock, fecha_vencimiento) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getCategoria());
            ps.setBigDecimal(4, p.getPrecioCompra());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getStock());
            asignarFecha(ps, 7, p);
            ps.executeUpdate();
        }
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET codigo_producto=?, nombre=?, categoria=?, "
                + "precio_compra=?, precio_venta=?, stock=?, fecha_vencimiento=? WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getCategoria());
            ps.setBigDecimal(4, p.getPrecioCompra());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getStock());
            asignarFecha(ps, 7, p);
            ps.setInt(8, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Producto> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos ORDER BY nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapear(rs);
        }
    }

    /** Busca por código, nombre o categoría (coincidencia parcial). */
    public List<Producto> buscar(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos "
                + "WHERE codigo_producto LIKE ? OR nombre LIKE ? OR categoria LIKE ? ORDER BY nombre";
        String patron = "%" + texto + "%";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        }
    }

    /**
     * Descuenta stock de forma atómica.
     * @return false si no hay stock suficiente (o el producto no existe).
     */
    public boolean descontarStock(int id, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id=? AND stock >= ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, id);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- auxiliares ----------

    private void asignarFecha(PreparedStatement ps, int indice, Producto p) throws SQLException {
        if (p.getFechaVencimiento() == null) {
            ps.setNull(indice, Types.DATE);
        } else {
            ps.setDate(indice, Date.valueOf(p.getFechaVencimiento()));
        }
    }

    private List<Producto> mapear(ResultSet rs) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        while (rs.next()) {
            Date f = rs.getDate("fecha_vencimiento");
            lista.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("codigo_producto"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getBigDecimal("precio_compra"),
                    rs.getBigDecimal("precio_venta"),
                    rs.getInt("stock"),
                    f == null ? null : f.toLocalDate()));
        }
        return lista;
    }
}
