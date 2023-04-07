package main

import (
	"fmt"
	camunda_client_go "github.com/citilinkru/camunda-client-go/v3"
	"github.com/citilinkru/camunda-client-go/v3/processor"
	"strings"
	"time"
)

func main() {
	client := camunda_client_go.NewClient(camunda_client_go.ClientOptions{
		EndpointUrl: "http://localhost:8080/engine-rest",
		Timeout:     time.Second * 10_000,
	})

	logger := func(err error) {
		fmt.Println(err.Error())
	}

	proc := processor.NewProcessor(client, &processor.Options{
		WorkerId:           "go-client",
		LockDuration:       time.Second * 50,
		MaxTasks:           10,
		LongPollingTimeout: 500 * time.Second,
	}, logger)

	processDefinitionId := "Process_external_asking"
	proc.AddHandler(
		[]*camunda_client_go.QueryFetchAndLockTopic{
			{TopicName: "ask_lower"},
			{ProcessDefinitionId: &processDefinitionId},
		},
		func(ctx *processor.Context) error {
			x3 := ctx.Task.Variables["x3"].Value.(string)
			fmt.Printf("x3=%s", x3)
			x4 := strings.ToLower(x3)

			err := ctx.Complete(processor.QueryComplete{
				Variables: &map[string]camunda_client_go.Variable{
					"x4": {Value: x4, Type: "string"},
				},
			})
			if err != nil {
				fmt.Printf("Error set complete task %s: %s\n", ctx.Task.Id, err)
				return ctx.HandleFailure(processor.QueryHandleFailure{})
			}

			fmt.Printf("Task %s completed\n", ctx.Task.Id)
			return nil
		},
	)
	select {}
}
