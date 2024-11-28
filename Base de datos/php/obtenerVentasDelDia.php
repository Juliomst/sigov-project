<?php
require_once 'conexion.php';

date_default_timezone_set('America/Hermosillo');

$query = "SELECT 
    v.id_venta, 
    v.fechaRegistro, 
    v.monto, 
    o.noMesa,
    CASE
        WHEN e.id_efectivo IS NOT NULL THEN 'efectivo'
        WHEN t.id_tarjeta IS NOT NULL THEN 'tarjeta'
        ELSE 'pendiente'
    END as metodoPago
FROM VENTAS v
LEFT JOIN ORDENES o ON v.id_orden = o.id_orden
LEFT JOIN PAGOS p ON v.id_venta = p.id_venta
LEFT JOIN TARJETAS t ON p.id_pago = t.id_pago
LEFT JOIN EFECTIVOS e ON p.id_pago = e.id_pago
WHERE DATE(v.fechaRegistro) = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
ORDER BY v.fechaRegistro DESC, v.id_venta DESC";

$stmt = $mysql->prepare($query);
$stmt->execute();
$resultado = $stmt->get_result();

$ventas = array();
$total_dia = 0;

// Obtiene la fecha actual en formato Y-m-d
$hoy = date('Y-m-d');

while($venta = $resultado->fetch_assoc()) {
    $total_dia += floatval($venta['monto']);
    
    // Formatea el monto a dos decimales
    $venta['monto'] = number_format(floatval($venta['monto']), 2, '.', '');
    $ventas[] = $venta;
}

// Cambia la condición de verificación de ventas
if(count($ventas) > 0) {
    echo json_encode(array(
        'status' => 'success',
        'ventas' => $ventas,
        'total_dia' => number_format($total_dia, 2, '.', ''),
        'fecha_actual' => $hoy,
        'debug_info' => array(
            'timezone' => date_default_timezone_get(),
            'server_time' => date('Y-m-d H:i:s')
        )
    ));
} else {
    echo json_encode(array(
        'status' => 'error',
        'message' => 'No hay ventas registradas hoy',
        'debug_info' => array(
            'timezone' => date_default_timezone_get(),
            'server_time' => date('Y-m-d H:i:s'),
            'query' => $query
        )
    ));
}

$mysql->close();
?>