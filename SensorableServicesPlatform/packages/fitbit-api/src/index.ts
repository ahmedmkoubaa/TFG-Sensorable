import express from "express"
const app = express()

app.get("/", (req, res) => {
  res.send("Hello from typescript")
  console.log(`received request: ${req}`)
})

const port = process.env.port || 3000

app.get("/file", (req, res) => {
  res.send("Hello from typescript")
  console.log(`received request: ${req}`)
})

app.listen(port, () => console.log(`App listening in port: ${port}`))
