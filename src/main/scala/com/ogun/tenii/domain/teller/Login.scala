package com.ogun.tenii.domain.teller

case class TellerResponse(
  name: String,
  links: Links,
  institution: String,
  id: String,
  enrollment_id: String,
  customer_type: String,
  currency: String,
  bank_code: String,
  balance: String,
  account_number: String
)

//"transactions": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21/transactions",
//"standing_orders": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21/standing_orders",
//"self": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21",
//"payments": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21/payments",
//"payees": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21/payees",
//"direct_debits": "https://api.teller.io/accounts/39252363-65d1-46e1-b9f7-07af3076bd21/direct_debits"
case class Links(transactions: String)

case class TellerFailure(cause: String)