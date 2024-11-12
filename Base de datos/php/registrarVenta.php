<?php

if($_SERVER["REQUEST_METHOD"] == "POST"){
    require_once 'conexion.php';

    $idOrden = $mysql->real_escape_string($_POST["idOrden"]);
    $monto = $mysql->real_escape_string($_POST["monto"]);
    $metodoPago = $mysql->real_escape_string($_POST["metodoPago"]);
    $fecha_actual = date('Y-m-d');

    $mysql->begin_transaction();

try{
    //Se inserta en la tabla VENTAS
    $queryVenta = "INSERT INTO VENTAS (fechaRegistro, monto, id_orden) VALUES (?, ?, ?)";
    $stmtVenta = $mysql->prepare($queryVenta);
    $stmtVenta->bind_param("sdi", $fecha_actual, $monto, $idOrden);
    $stmtVenta->execute();

    $idVenta = $mysql->insert_id;

    //Se inserta en la tabla PAGOS
    $queryPago = "INSERT INTO PAGOS (montoEfectuado, id_venta) VALUES (?, ?)";
    $stmtPago = $mysql->prepare($queryPago);
    $stmtPago->bind_param("di", $monto, $idVenta);
    $stmtPago->execute();

    $idPago = $mysql->insert_id;

    if($metodoPago == "efectivo"){
        $queryEfectivo = "INSERT INTO EFECTIVOS (divisa, id_pago) VALUES ('MXN', ?)";
        $stmtEfectivo = $mysql->prepare($queryEfectivo);
        $stmtEfectivo->bind_param("i", $idPago);
        $stmtEfectivo->execute();
    }else{
        $banco = $mysql->real_escape_string($_POST["banco"]);
        $folio = $mysql->real_escape_string($_POST["folio"]);

        $queryTarjeta = "INSERT INTO TARJETAS (banca, folio, id_pago) VALUES (?, ?, ?)";
        $stmtTarjeta = $mysql->prepare($queryTarjeta);
        $stmtTarjeta->bind_param("ssi", $banco, $folio, $idPago);
        $stmtTarjeta->execute();
    }
    $mysql->commit();

    echo json_encode(array(
        'status'=>'success',
        'message'=>'Venta registrada exitosamente'
    ));
}catch(Exception $e){
    $mysql->rollback();
    echo json_encode(array(
        'status'=>'error',
        'message'=>'Error al registrar la venta: ' . $e->getMessage()
    ));
    }
    $mysql->close();
}
?>