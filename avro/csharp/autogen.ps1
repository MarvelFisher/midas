& D:

& cd D:\TaibeiWorkSpace\LTS\avro\idl

Get-ChildItem -Recurse -Include *.avsc | % {

    Write-Host "Processing '$_' ..."

    & D:\TaibeiWorkSpace\LTS\avro\csharp\codegen\Release\avrogen.exe -s $_.FullName D:\TaibeiWorkSpace\LTS\avro\csharp

}