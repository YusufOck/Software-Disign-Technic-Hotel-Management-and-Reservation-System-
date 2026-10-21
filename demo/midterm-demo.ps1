$ErrorActionPreference = 'Stop'

$apiBase = 'http://localhost:8080/api'
$jsonHeaders = @{ 'Content-Type' = 'application/json' }

function Invoke-JsonPost {
    param(
        [Parameter(Mandatory)] [string] $Path,
        [Parameter(Mandatory)] [hashtable] $Body
    )

    Invoke-RestMethod -Method Post -Uri ($apiBase + $Path) -Headers $jsonHeaders `
        -Body ($Body | ConvertTo-Json -Depth 6)
}

function Invoke-ExpectedConflict {
    param(
        [Parameter(Mandatory)] [scriptblock] $Request,
        [Parameter(Mandatory)] [string] $Scenario
    )

    try {
        & $Request | Out-Null
        throw "$Scenario unexpectedly succeeded."
    }
    catch {
        if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 409) {
            Write-Host "[PASS] $Scenario returned HTTP 409." -ForegroundColor Green
            return
        }
        throw
    }
}

$unique = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$today = (Get-Date).Date
$futureCheckIn = $today.AddDays(2)
while ($futureCheckIn.DayOfWeek -ne [DayOfWeek]::Friday) {
    $futureCheckIn = $futureCheckIn.AddDays(1)
}
$futureCheckOut = $futureCheckIn.AddDays(3)

Write-Host 'Creating room type, rooms and guest...'
$roomType = Invoke-JsonPost '/room-types' @{
    name = "Demo Double $unique"
    basePrice = 100.00
    capacity = 2
}
$roomA = Invoke-JsonPost '/rooms' @{ number = "DEMO-A-$unique"; roomTypeId = $roomType.id }
$roomB = Invoke-JsonPost '/rooms' @{ number = "DEMO-B-$unique"; roomTypeId = $roomType.id }
$guest = Invoke-JsonPost '/guests' @{
    name = 'Ada Demo'
    email = "ada.$unique@example.com"
    preferences = 'Quiet room'
}

Write-Host 'Creating a weekend-inclusive reservation with two extras...'
$firstReservation = Invoke-JsonPost '/reservations' @{
    guestId = $guest.id
    roomId = $roomA.id
    checkIn = $futureCheckIn.ToString('yyyy-MM-dd')
    checkOut = $futureCheckOut.ToString('yyyy-MM-dd')
    extras = @('BREAKFAST', 'PARKING')
    simulatePaymentSuccess = $true
}

if ($firstReservation.status -ne 'CONFIRMED' -or $firstReservation.priceLines.Count -ne 3) {
    throw 'The priced reservation did not produce the expected confirmation and three price lines.'
}
Write-Host "[PASS] Confirmed reservation: total=$($firstReservation.totalPrice), strategy=$($firstReservation.pricingStrategy), lines=$($firstReservation.priceLines.Count)." -ForegroundColor Green

Invoke-ExpectedConflict -Scenario 'Overlapping reservation' -Request {
    Invoke-JsonPost '/reservations' @{
        guestId = $guest.id
        roomId = $roomA.id
        checkIn = $futureCheckIn.AddDays(1).ToString('yyyy-MM-dd')
        checkOut = $futureCheckOut.ToString('yyyy-MM-dd')
        extras = @()
        simulatePaymentSuccess = $true
    }
}

$adjacentReservation = Invoke-JsonPost '/reservations' @{
    guestId = $guest.id
    roomId = $roomA.id
    checkIn = $futureCheckOut.ToString('yyyy-MM-dd')
    checkOut = $futureCheckOut.AddDays(1).ToString('yyyy-MM-dd')
    extras = @()
    simulatePaymentSuccess = $true
}
Write-Host "[PASS] Adjacent reservation accepted with status $($adjacentReservation.status)." -ForegroundColor Green

$stateReservation = Invoke-JsonPost '/reservations' @{
    guestId = $guest.id
    roomId = $roomB.id
    checkIn = $today.ToString('yyyy-MM-dd')
    checkOut = $today.AddDays(1).ToString('yyyy-MM-dd')
    extras = @()
    simulatePaymentSuccess = $true
}

Invoke-ExpectedConflict -Scenario 'Checkout before check-in' -Request {
    Invoke-RestMethod -Method Post -Uri "$apiBase/reservations/$($stateReservation.id)/check-out"
}

$checkedIn = Invoke-RestMethod -Method Post -Uri "$apiBase/reservations/$($stateReservation.id)/check-in"
$completed = Invoke-RestMethod -Method Post -Uri "$apiBase/reservations/$($stateReservation.id)/check-out"
if ($checkedIn.status -ne 'CHECKED_IN' -or $completed.status -ne 'COMPLETED') {
    throw 'The State transition scenario did not finish as expected.'
}
Write-Host '[PASS] State transitions reached CHECKED_IN and COMPLETED.' -ForegroundColor Green

$notifications = Invoke-RestMethod -Method Get -Uri "$apiBase/notifications?guestId=$($guest.id)"
$audit = Invoke-RestMethod -Method Get -Uri "$apiBase/audit"
if ($notifications.Count -lt 4 -or $audit.Count -lt 6) {
    throw 'Observer evidence is incomplete.'
}
Write-Host "[PASS] Observer evidence: $($notifications.Count) notifications and $($audit.Count) audit events." -ForegroundColor Green
Write-Host 'MIDTERM DEMO PASSED.' -ForegroundColor Cyan
