& C:

& cd C:\workspace_t\LTS\avro\idl

Get-ChildItem -Recurse -Include *.avsc | % {

    Write-Host "Processing '$_' ..."

    & C:\workspace_t\LTS\avro\csharp\codegen\Release\avrogen.exe -s $_.FullName C:\workspace_t\LTS\avro\csharp\

}