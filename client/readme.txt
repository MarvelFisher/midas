                     ===============
                     = FDT LTS API =
                     ===============

>>PREFACE

FDT LTS API is a software framework that provides programming access
to core trading functions on FDT market leading products:

	ForexMaster（外匯操盤手）
	FuturesMaster（期貨操盤手）
	StockMaster（股市操盤手）

Through FDT LTS API, users may send orders to FDT's prominent electronic
trading platform LTS and route to any market in the world. One advantage
of using API is to allow users develop their trading strategy and execute
it automatically without human intervention.

FDT LTS API is not just a traditional API, it is an event
driven framework that allows faster responding time and more functionalities
to be exposed in the future.


>>BASIC

Medium level knowledge of java programming is required to use FDT LTS API. 
Experience with Eclipse IDE will also help.

The communication with LTS platform and API is done through exchanging of
events

Based on the communication of direction, there are two type of events:

From Event - event sent from LTS
To Event - event send by API

Some events must be handled before the program can go into a standard trading
mode. They are listed as following:

ServerReadyEvent(From Event) - this is the first event API will receive when it
is connected to LTS. Typical handling is to respond with a UserLoginEvent.

UserLoginEvent(To Event) - user must login with a correct user id and password before
performing any subsequent request events.

UserLoginReplyEvent(From Event) - when LTS receives a UserLoginEvent, it will respond
with this event to indicate whether the login is successful.

SystemErrorEvent(From Event) - LTS informS API when there is critical error.

>>EXAMPLES

The best way of learning API is to look at existing example programs. There are two
examples included in the package:

LtsApiAdaptor

- shows the subscription and handling of events
- shows login process
- shows enter/amend/cancel orders
- developer may change conf/api.xml to customise settings
including user id and password

LtsWsFrame
- a more complicate example
- shows a GUI
- user may login through GUI
- user may enter/amend/cancel order through GUI
- user may inspect account and position details in the GUI
- developer may change conf/apigui.xml to customise settings


