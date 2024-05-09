Set-Location server
Start-Job -ScriptBlock{
    go run server.go
}
Set-Location ..

$numberOfUser = 2

for ($i = 0; $i -lt $numberOfUser; $i++) {
    Start-Job -ScriptBlock{
        mvn clean javafx:run
    }
}

#Get-Job | Stop-Job