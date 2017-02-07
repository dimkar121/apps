<?php
header('Content-type: application/json');
$q = $_GET['author_Query'];
$s = $_GET['simPer'];

//echo $q."   ".$s;
$service_url = "http://localhost:4443/JSON/dblp?author_Query=".$q."&simPer=".$s."&maxNoRes=50";
$curl = curl_init($service_url);
curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
$curl_response = curl_exec($curl);
if ($curl_response === false) {
    $info = curl_getinfo($curl);
    curl_close($curl);
    die('error occured during curl exec. Additional info: ' . var_export($info));
}
curl_close($curl);
echo $curl_response;

?>
