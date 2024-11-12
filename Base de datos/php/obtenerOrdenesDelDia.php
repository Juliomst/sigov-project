<?php

require_once 'conexion.php';

// Se asegura de usar la zona horaria correcta
date_default_timezone_set('America/Hermosillo'); //Cambiar la zona horaria
$fecha_actual = date('Y-m-d');

// Se Modifica la consulta para ver mejor los errores
$query = "SELECT o.id_orden, o.noMesa, o.descripcion, o.fechaRegistro
        FROM ORDENES o
        LEFT JOIN VENTAS v ON o.id_orden = v.id_orden
        WHERE DATE(o.fechaRegistro) = ?
        AND v.id_venta IS NULL
        ORDER BY o.fechaRegistro DESC";

$stmt = $mysql->prepare($query);
$stmt->bind_param("s", $fecha_actual);
$stmt->execute();
$resultado = $stmt->get_result();

$ordenes = array();
while($orden = $resultado->fetch_assoc()){
    // debugging
    $orden['fecha_actual'] = $fecha_actual;
    $orden['fecha_registro_raw'] = $orden['fechaRegistro'];
    $ordenes[] = $orden;
}

if(count($ordenes) > 0){
    echo json_encode(array(
        'status'=>'success',
        'ordenes'=>$ordenes,
        'debug' => array(
            'fecha_actual' => $fecha_actual,
            'total_ordenes' => count($ordenes)
        )
    ));
}else{
    // más información de debug
    $debug_query = "SELECT COUNT(*) as total, 
                          MIN(fechaRegistro) as primera_fecha, 
                          MAX(fechaRegistro) as ultima_fecha 
                   FROM ORDENES";
    $debug_result = $mysql->query($debug_query);
    $debug_data = $debug_result->fetch_assoc();
    
    echo json_encode(array(
        'status'=>'error',
        'message'=>'No hay órdenes para el día de hoy',
        'debug' => array(
            'fecha_actual' => $fecha_actual,
            'total_ordenes_sistema' => $debug_data['total'],
            'primera_fecha' => $debug_data['primera_fecha'],
            'ultima_fecha' => $debug_data['ultima_fecha']
        )
    ));
}

$mysql->close();
?>