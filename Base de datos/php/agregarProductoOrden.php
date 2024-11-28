<?php
header('Content-Type: application/json');

// Función para logging
function logError($message) {
    error_log("[SIGOV Debug] " . $message);
}

function sendResponse($status, $message) {
    echo json_encode([
        'status' => $status,
        'message' => $message
    ]);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] != "POST") {
    logError("Método inválido: " . $_SERVER["REQUEST_METHOD"]);
    sendResponse('error', 'Método no permitido');
}

// Log de todos los datos POST recibidos
logError("Datos POST recibidos: " . print_r($_POST, true));

// Valida parámetros requeridos
$required_params = ['id_orden', 'id_producto', 'cantidadProducto'];
foreach ($required_params as $param) {
    if (!isset($_POST[$param]) || empty(trim($_POST[$param]))) {
        logError("Falta parámetro requerido: " . $param);
        sendResponse('error', "Falta el parámetro requerido: $param");
    }
}

require_once 'conexion.php';

try {
    $mysql->begin_transaction();

    $idOrden = $mysql->real_escape_string($_POST["id_orden"]);
    $idProducto = $mysql->real_escape_string($_POST["id_producto"]);
    $cantidadProducto = $mysql->real_escape_string($_POST["cantidadProducto"]);

    logError("Valores procesados - idOrden: $idOrden, idProducto: $idProducto, cantidad: $cantidadProducto");

    // Valida que los valores sean numéricos
    if (!is_numeric($idOrden) || !is_numeric($idProducto) || !is_numeric($cantidadProducto)) {
        throw new Exception("Los valores proporcionados no son válidos");
    }

    // Valida que la orden existe y muestra detalles
    $checkOrden = $mysql->query("SELECT * FROM ORDENES WHERE id_orden = $idOrden");
    logError("Check orden result: " . $checkOrden->num_rows . " filas encontradas");
    if ($checkOrden->num_rows === 0) {
        throw new Exception("La orden $idOrden no existe");
    }

    // Valida que el producto existe y muestra detalles
    $checkProducto = $mysql->query("SELECT * FROM PRODUCTOS WHERE id_producto = $idProducto");
    logError("Check producto result: " . $checkProducto->num_rows . " filas encontradas");
    if ($checkProducto->num_rows === 0) {
        throw new Exception("El producto $idProducto no existe");
    }

    // Verifica si el producto ya existe en la orden
    $checkExistente = $mysql->query(
        "SELECT id_orden FROM ORDEN_PRODUCTO 
         WHERE id_orden = $idOrden AND id_producto = $idProducto"
    );
    logError("Check existente result: " . $checkExistente->num_rows . " filas encontradas");
    
    if ($checkExistente->num_rows > 0) {
        throw new Exception("Este producto ya está en la orden");
    }

    // Agrega descripción si existe
    if (isset($_POST["descripcion"]) && trim($_POST["descripcion"]) !== "") {
        $descripcion = $mysql->real_escape_string($_POST["descripcion"]);
        $updateOrdenQuery = "UPDATE ORDENES SET descripcion = CONCAT(IFNULL(descripcion, ''), '\n', ?) WHERE id_orden = ?";
        $stmtOrden = $mysql->prepare($updateOrdenQuery);
        $stmtOrden->bind_param("si", $descripcion, $idOrden);
        if (!$stmtOrden->execute()) {
            logError("Error al actualizar descripción: " . $stmtOrden->error);
            throw new Exception("Error al actualizar la descripción: " . $stmtOrden->error);
        }
        logError("Descripción actualizada exitosamente");
    }

    // Inserta el producto en la orden
    $query = "INSERT INTO ORDEN_PRODUCTO (id_orden, id_producto, cantidadProducto) VALUES (?, ?, ?)";
    logError("Query a ejecutar: $query");
    
    $stmt = $mysql->prepare($query);
    if (!$stmt) {
        logError("Error en prepare: " . $mysql->error);
        throw new Exception("Error al preparar la consulta: " . $mysql->error);
    }
    
    $stmt->bind_param("iii", $idOrden, $idProducto, $cantidadProducto);
    logError("Parámetros vinculados - ejecutando query");
    
    if (!$stmt->execute()) {
        logError("Error en execute: " . $stmt->error);
        throw new Exception("Error al agregar el producto: " . $stmt->error);
    }
    
    logError("Producto agregado exitosamente. Affected rows: " . $stmt->affected_rows);

    $mysql->commit();
    sendResponse('success', 'Producto agregado exitosamente');

} catch (Exception $e) {
    logError("Exception: " . $e->getMessage());
    $mysql->rollback();
    sendResponse('error', $e->getMessage());
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($stmtOrden)) $stmtOrden->close();
    $mysql->close();
}
?>