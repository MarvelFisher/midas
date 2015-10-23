quote:([]time:`timestamp$(); sym:`g#`symbol$(); bid:`float$(); ask:`float$(); bsize:`float$(); asize:`float$(); price:`float$(); size:`float$(); turnover:`float$(); high:`float$(); low:`float$(); open:`float$(); close:`float$(); totalsize:`float$())

/timestamp -> timespan

/quote:([]time:`timespan$(); sym:`g#`symbol$(); bid:`float$(); ask:`float$(); bsize:`long$(); asize:`long$(); mode:`char$(); ex:`char$())
/trade:([]time:`timespan$(); sym:`g#`symbol$(); price:`float$(); size:`int$(); stop:`boolean$(); cond:`char$(); ex:`char$())
