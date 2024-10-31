<?php
header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once 'conexion.php';
    
    error_log("Datos POST recibidos: " . print_r($_POST, true));
    
    try {
        $query = isset($_POST['query']) ? $mysql->real_escape_string($_POST['query']) : '';
        
        $sql = "SELECT * FROM PRODUCTOS WHERE nombre LIKE ?";
        
        if ($stmt = $mysql->prepare($sql)) {
            $searchParam = "%$query%";
            $stmt->bind_param("s", $searchParam);
            
            if ($stmt->execute()) {
                $result = $stmt->get_result();
                $productos = array();
                
                while ($row = $result->fetch_assoc()) {
                    $productos[] = array(
                        'id_producto' => $row['id_producto'],
                        'nombre' => $row['nombre'],
                        'precio' => $row['precio'],
                        'tipoProducto' => $row['tipoProducto'],
                        'descripcion' => $row['descripcion']
                    );
                }
                
                echo json_encode($productos);
                
            } else {
                error_log("Error en execute: " . $stmt->error);
                echo json_encode([]);
            }
            
            $stmt->close();
        } else {
            error_log("Error en prepare: " . $mysql->error);
            echo json_encode([]);
        }
        
    } catch (Exception $e) {
        error_log("Exception: " . $e->getMessage());
        echo json_encode([]);
    }
    
    $mysql->close();
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Método no permitido'
    ]);
}
?>