<?php
header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';
    
    // Log de los datos recibidos
    error_log("Datos POST recibidos: " . print_r($_POST, true));
    
    // Verificar que todos los campos necesarios estén presentes
    $requiredFields = ['nombre', 'precio', 'tipoProducto', 'descripcion'];
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
        $nombre = $mysql->real_escape_string($_POST["nombre"]);
        $precio = floatval($mysql->real_escape_string($_POST["precio"]));
        $tipoProducto = $mysql->real_escape_string($_POST["tipoProducto"]);
        $descripcion = $mysql->real_escape_string($_POST["descripcion"]);
        
        // Validar el tipo de producto
        $tiposValidos = ['Comida', 'Bebida', 'Postre'];
        if (!in_array($tipoProducto, $tiposValidos)) {
            error_log("Tipo de producto no válido: $tipoProducto");
            echo json_encode([
                'status' => 'error',
                'message' => 'Tipo de producto no válido. Debe ser: Comida, Bebida o Postre'
            ]);
            exit;
        }
        
        $query = "INSERT INTO PRODUCTOS (nombre, precio, tipoProducto, descripcion) VALUES (?, ?, ?, ?)";
        
        if ($stmt = $mysql->prepare($query)) {
            $stmt->bind_param("sdss", $nombre, $precio, $tipoProducto, $descripcion);
            
            error_log("Ejecutando query con valores - Nombre: $nombre, Precio: $precio, Tipo: $tipoProducto, Desc: $descripcion");
            
            if ($stmt->execute()) {
                echo json_encode([
                    'status' => 'success',
                    'message' => 'Producto registrado exitosamente',
                    'id' => $mysql->insert_id,
                    'datos' => [
                        'nombre' => $nombre,
                        'precio' => $precio,
                        'tipoProducto' => $tipoProducto,
                        'descripcion' => $descripcion
                    ]
                ]);
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