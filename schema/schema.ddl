# 
# Copy to clipboard, Create Statement
# 

CREATE DATABASE `optionsdb` /*!40100 DEFAULT CHARACTER SET utf8 */;

CREATE TABLE `holiday` (
  `holiday` date NOT NULL,
  `name` varchar(75) DEFAULT NULL,
  PRIMARY KEY (`holiday`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `options_expirations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `symbol` varchar(5) NOT NULL,
  `expiration` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;

CREATE TABLE `spx` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `symbol` text,
  `exchange` text,
  `trade date` date DEFAULT NULL,
  `adjusted stock close price` double DEFAULT NULL,
  `option symbol` text,
  `expiration` date DEFAULT NULL,
  `strike` int(11) DEFAULT NULL,
  `call/put` text,
  `style` text,
  `ask` double DEFAULT NULL,
  `bid` double DEFAULT NULL,
  `mean price` double DEFAULT NULL,
  `iv` double DEFAULT NULL,
  `volume` int(11) DEFAULT NULL,
  `open interest` int(11) DEFAULT NULL,
  `stock price for iv` double DEFAULT NULL,
  `*` text,
  `delta` double DEFAULT NULL,
  `vega` double DEFAULT NULL,
  `gamma` double DEFAULT NULL,
  `theta` double DEFAULT NULL,
  `rho` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_spx_trade_date` (`trade date`),
  KEY `idx_spx_expiration` (`expiration`)
) ENGINE=InnoDB AUTO_INCREMENT=1787513 DEFAULT CHARSET=utf8;

CREATE TABLE `spx_last_nt` (
  `TradeDate` date NOT NULL,
  `Open` double DEFAULT NULL,
  `High` double DEFAULT NULL,
  `Low` double DEFAULT NULL,
  `Close` double DEFAULT NULL,
  `Volume` int(11) DEFAULT NULL,
  PRIMARY KEY (`TradeDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `trade` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exec_time` date NOT NULL,
  `exp` date NOT NULL,
  `exp2` date DEFAULT NULL COMMENT 'If you''re doing calendar, this is the second expiration',
  `trade_type` varchar(25) NOT NULL COMMENT 'Iron Condor, Butterfly, Strangle, Straddle, Vertical and Calendar',
  `opening_cost` double NOT NULL,
  `closing_cost` double DEFAULT NULL,
  `profit` double DEFAULT NULL,
  `close_status` varchar(45) DEFAULT NULL,
  `close_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=907 DEFAULT CHARSET=utf8;

CREATE TABLE `trade_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trade_id` int(11) NOT NULL,
  `exec_time` date NOT NULL,
  `side` varchar(10) NOT NULL DEFAULT 'SELL' COMMENT 'BUY or SELL',
  `qty` int(11) NOT NULL DEFAULT '1',
  `pos_effect` varchar(10) NOT NULL DEFAULT 'OPENING' COMMENT 'OPENING or CLOSING',
  `symbol` varchar(10) NOT NULL,
  `exp` date NOT NULL,
  `strike` double NOT NULL,
  `type` varchar(10) NOT NULL DEFAULT 'CALL' COMMENT 'PUT or CALL',
  `price` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `trade_id_fk_idx` (`trade_id`),
  CONSTRAINT `trade_id_fk_idx` FOREIGN KEY (`trade_id`) REFERENCES `trade` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2743 DEFAULT CHARSET=utf8 COMMENT='This table is based off of TOS''s Trade History Account Statement page';

$host="localhost";
$port=3306;
$socket="";
$user="root";
$password="";
$dbname="optionsdb";

$con = new mysqli($host, $user, $password, $dbname, $port, $socket)
	or die ('Could not connect to the database server' . mysqli_connect_error());

//$con->close();

$query = "SELECT * FROM optionsdb.spx";


if ($stmt = $con->prepare($query)) {
    $stmt->execute();
    $stmt->bind_result($field1, $field2);
    while ($stmt->fetch()) {
        //printf("%s, %s\n", $field1, $field2);
    }
    $stmt->close();
}
