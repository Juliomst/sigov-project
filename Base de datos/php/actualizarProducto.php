<?php
header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';
    
    error_log("Datos POST recibidos: " . print_r($_POST, true));
    
    // Verifica que todos los campos necesarios estén presentes
    $requiredFields = ['id_producto', 'nombre', 'precio', 'tipoProducto', 'descripcion'];
    foreach ($requiredFields as $field) {
        if (!isset($_POST[$field]) || empty($_POST[$field])) {
            echo json_encode([
                'status' => 'error',
                'message' => "El campo $field es requerido"
            ]);
            error_log("Campo faltante: $field");
            exit;
        }
    }
    
    try {
        $id_producto = $mysql->real_escape_string($_POST["id_producto"]);
        $nombre = $mysql->real_escape_string($_POST["nombre"]);
        $precio = floatval($mysql->real_escape_string($_POST["precio"]));
        $tipoProducto = $mysql->real_escape_string($_POST["tipoProducto"]);
        $descripcion = $mysql->real_escape_string($_POST["descripcion"]);
        
        // Valida el tipo de producto
        $tiposValidos = ['Comida', 'Bebida', 'Postre'];
        if (!in_array($tipoProducto, $tiposValidos)) {
            error_log("Tipo de producto no válido: $tipoProducto");
            echo json_encode([
                'status' => 'error',
                'message' => 'Tipo de producto no válido. Debe ser: Comida, Bebida o Postre'
            ]);
            exit;
        }
        
        $query = "UPDATE PRODUCTOS SET nombre=?, precio=?, tipoProducto=?, descripcion=? WHERE id_producto=?";
        
        if ($stmt = $mysql->prepare($query)) {
            $stmt->bind_param("sdssi", $nombre, $precio, $tipoProducto, $descripcion, $id_producto);
            
            if ($stmt->execute()) {
                if ($stmt->affected_rows > 0) {
                    echo json_encode([
                        'status' => 'success',
                        'message' => 'Producto actualizado exitosamente'
                    ]);
                } else {
                    echo json_encode([
                        'status' => 'error',
                        'message' => 'No se encontró el producto o no hubo cambios'
                    ]);
                }
            } else {
                error_log("Error en execute: " . $stmt->error);
                echo json_encode([
                    'status' => 'error',
                    'message' => 'Error al ejecutar la consulta: ' . $stmt->error
                ]);
            }
            $stmt->close();
        } else {
            error_log("Error en prepare: " . $mysql->error);
            echo json_encode([
                'status' => 'error',
                'message' => 'Error al preparar la consulta: ' . $mysql->error
            ]);
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