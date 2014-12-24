DROP TABLE ANYONE.EXECUTIONS;
CREATE TABLE ANYONE.EXECUTIONS(OBJECT_ID VARCHAR(40) NOT NULL PRIMARY KEY, SYMBOL VARCHAR(30), SIDE VARCHAR(20), QUANTITY DOUBLE, PRICE DOUBLE,
	ORDER_ID VARCHAR(40), PARENT_ID VARCHAR(40), STRATEGY_ID VARCHAR(40), EXEC_ID VARCHAR(40), MODIFIED TIMESTAMP, CREATED TIMESTAMP, SEQ_ID VARCHAR(40), SERVER_ID VARCHAR(40), TRADER VARCHAR(40), ACCOUNT VARCHAR(40));

DROP TABLE ANYONE.ACTIVE_CHILD_ORDERS;
CREATE TABLE ANYONE.ACTIVE_CHILD_ORDERS(OBJECT_ID VARCHAR(40) NOT NULL PRIMARY KEY, SYMBOL VARCHAR(30), SIDE VARCHAR(20), QUANTITY DOUBLE, PRICE DOUBLE,
	CUM_QTY DOUBLE, AVG_PX DOUBLE, ORDER_TYPE VARCHAR(20), ORD_STATUS VARCHAR(20), PARENT_ID VARCHAR(40), STRATEGY_ID VARCHAR(40), 
	MODIFIED TIMESTAMP, CREATED TIMESTAMP, SEQ_ID VARCHAR(40), CLORDER_ID VARCHAR(40), SERVER_ID VARCHAR(40), TRADER VARCHAR(40), ACCOUNT VARCHAR(40));

DROP TABLE ANYONE.CHILD_ORDER_AUDIT;
CREATE TABLE ANYONE.CHILD_ORDER_AUDIT(AUDIT_ID VARCHAR(40) NOT NULL PRIMARY KEY,OBJECT_ID VARCHAR(40), EXEC_TYPE VARCHAR(20), SYMBOL VARCHAR(30), SIDE VARCHAR(20), QUANTITY DOUBLE, PRICE DOUBLE,
	CUM_QTY DOUBLE, AVG_PX DOUBLE, ORDER_TYPE VARCHAR(20), ORD_STATUS VARCHAR(20), PARENT_ID VARCHAR(40), STRATEGY_ID VARCHAR(40), 
	MODIFIED TIMESTAMP, CREATED TIMESTAMP, SEQ_ID VARCHAR(40), CLORDER_ID VARCHAR(40), SERVER_ID VARCHAR(40), TRADER VARCHAR(40), ACCOUNT VARCHAR(40));

DROP TABLE ANYONE.TEXT_OBJECTS;
CREATE TABLE ANYONE.TEXT_OBJECTS(OBJECT_ID VARCHAR(40), OBJECT_TYPE VARCHAR(40), STRATEGY_STATE VARCHAR(20), LINE_NO INTEGER, 
	TIME_STAMP TIMESTAMP, XML_TEXT VARCHAR(4096), 
	SERVER_ID VARCHAR(40),
	CONSTRAINT ST_PRIMARY_KEY PRIMARY KEY (OBJECT_ID, OBJECT_TYPE, LINE_NO));
CREATE INDEX ST_INDEX ON ANYONE.TEXT_OBJECTS (OBJECT_ID);

DROP TABLE ANYONE.USERS;
CREATE TABLE ANYONE.USERS(USER_ID VARCHAR(40) NOT NULL PRIMARY KEY, USER_NAME VARCHAR(40), PASSWORD VARCHAR(40),
	EMAIL VARCHAR(80), PHONE VARCHAR(40), CREATED TIMESTAMP, LAST_LOGIN TIMESTAMP, USER_TYPE VARCHAR(20), DEFAULT_ACCOUNT VARCHAR(40));

DROP TABLE ANYONE.ACCOUNTS;
CREATE TABLE ANYONE.ACCOUNTS(ACCOUNT_ID VARCHAR(40) NOT NULL PRIMARY KEY, USER_ID VARCHAR(40), MARKET VARCHAR(40), 
	CASH DOUBLE, MARGIN DOUBLE, PNL DOUBLE, ALL_TIME_PNL DOUBLE, UR_PNL DOUBLE, 
	CASH_DEPOSITED DOUBLE, ROLL_PRICE DOUBLE, UNIT_PRICE DOUBLE,
	ACTIVE BOOLEAN, CREATED TIMESTAMP, CURRENCY VARCHAR(10));
	
DROP TABLE ANYONE.ACCOUNTS_DAILY;
CREATE TABLE ANYONE.ACCOUNTS_DAILY(ACCOUNT_ID VARCHAR(40), USER_ID VARCHAR(40), MARKET VARCHAR(40), 
	CASH DOUBLE, MARGIN DOUBLE, PNL DOUBLE, ALL_TIME_PNL DOUBLE, UR_PNL DOUBLE, 
	CASH_DEPOSITED DOUBLE, ROLL_PRICE DOUBLE, UNIT_PRICE DOUBLE,
	ACTIVE BOOLEAN, CREATED TIMESTAMP, CURRENCY VARCHAR(10), ON_DATE DATE, ON_TIME TIMESTAMP,
	CONSTRAINT AD_PRIMARY_KEY PRIMARY KEY (ACCOUNT_ID, ON_DATE));
CREATE INDEX AD_INDEX ON ANYONE.ACCOUNTS_DAILY (ACCOUNT_ID);
	
DROP TABLE ANYONE.OPEN_POSITIONS;
CREATE TABLE ANYONE.OPEN_POSITIONS(POSITION_ID VARCHAR(40) NOT NULL PRIMARY KEY, USER_ID VARCHAR(40), ACCOUNT_ID VARCHAR(40),
	SYMBOL VARCHAR(40), QTY DOUBLE,	PRICE DOUBLE, PNL DOUBLE, AC_PNL DOUBLE, CREATED TIMESTAMP);

DROP TABLE ANYONE.CLOSED_POSITIONS;
CREATE TABLE ANYONE.CLOSED_POSITIONS(POSITION_ID VARCHAR(80) NOT NULL PRIMARY KEY, USER_ID VARCHAR(40), 
	ACCOUNT_ID VARCHAR(40), SYMBOL VARCHAR(40), 
	QTY DOUBLE,	BUY_PRICE DOUBLE, SELL_PRICE DOUBLE, PNL DOUBLE, AC_PNL DOUBLE, CREATED TIMESTAMP);	
	
DROP TABLE ANYONE.ACCOUNT_SETTINGS;
CREATE TABLE ANYONE.ACCOUNT_SETTINGS(ACCOUNT_ID VARCHAR(40) NOT NULL PRIMARY KEY, DEFAULT_QTY DOUBLE DEFAULT NULL, STOP_LOSS_VALUE DOUBLE DEFAULT NULL, COMPANY_SL_VALUE DOUBLE DEFAULT 0);   
	