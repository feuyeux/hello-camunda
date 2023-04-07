import { Client, logger, Variables } from "camunda-external-task-client-js"

const config = {
    baseUrl: 'http://localhost:8080/engine-rest',
    use: logger,
    asyncResponseTimeout: 10_000
}

const client = new Client(config)

client.subscribe('ask_prefix', { processDefinitionKey: "Process_external_asking" },
    async function ({ task, taskService }) {
        const x2 = task.variables.get('x2')
        const x3 = task.executionId +"_"+ x2
        console.log(`x2:${x2} x3:${x3}`)
        const processVariables = new Variables().set("x3", x3)
        try {
            await taskService.complete(task, processVariables)
            console.log("Task completed")
        } catch (e) {
            console.error(`Task failed,${e}`)
        }
    })