<?php
header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';
    
    error_log("Datos POST recibidos: " . print_r($_POST, true));
    
    if (!isset($_POST['id_producto']) || empty($_POST['id_producto'])) {
        echo json_encode([
            'status' => 'error',
            'message' => 'El ID del producto es requerido'
        ]);
        exit;
    }
    
    try {
        $id_producto = $mysql->real_escape_string($_POST["id_producto"]);
        
        // Inicia una transacción para asegurar la integridad de los datos
        $mysql->begin_transaction();
        
        try {
            // Primero elimina todas las referencias en ORDEN_PRODUCTO
            $deleteReferencesQuery = "DELETE FROM ORDEN_PRODUCTO WHERE id_producto = ?";
            if ($stmtRefs = $mysql->prepare($deleteReferencesQuery)) {
                $stmtRefs->bind_param("i", $id_producto);
                if (!$stmtRefs->execute()) {
                    throw new Exception("Error al eliminar referencias: " . $stmtRefs->error);
                }
                $stmtRefs->close();
            } else {
                throw new Exception("Error al preparar eliminación de referencias: " . $mysql->error);
            }
            
            // Luego elimina el producto
            $deleteProductQuery = "DELETE FROM PRODUCTOS WHERE id_producto = ?";
            if ($stmtProduct = $mysql->prepare($deleteProductQuery)) {
                $stmtProduct->bind_param("i", $id_producto);
                
                if ($stmtProduct->execute()) {
                    if ($stmtProduct->affected_rows > 0) {
                        // Si todo fue exitoso, confirma la transacción
                        $mysql->commit();
                        echo json_encode([
                            'status' => 'success',
                            'message' => 'Producto eliminado exitosamente'
                        ]);
                    } else {
                        $mysql->rollback();
                        echo json_encode([
                            'status' => 'error',
                            'message' => 'No se encontró el producto'
                        ]);
                    }
                } else {
                    throw new Exception("Error al eliminar producto: " . $stmtProduct->error);
                }
                $stmtProduct->close();
            } else {
                throw new Exception("Error al preparar eliminación de producto: " . $mysql->error);
            }
            
        } catch (Exception $e) {
            $mysql->rollback();
            throw $e;
        }
        
    } catch (Exception $e) {
        error_log("Exception: " . $e->getMessage());
        echo json_encode([
            'status' => 'error',
            'message' => 'Error en el servidor: ' . $e->getMessage()
        ]);
    }
    
    $mysql->close();
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Método no permitido'
    ]);
}
?>