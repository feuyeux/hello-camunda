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
		ApiUser:     "admin",
		ApiPassword: "123456",
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

	proc.AddHandler(
		[]*camunda_client_go.QueryFetchAndLockTopic{
			{TopicName: ""},
		},
		func(ctx *processor.Context) error {
			x := ctx.Task.Variables["x"].Value.(string)
			t := ctx.Task.Variables["x"].Type
			i := ctx.Task.Variables["x"].ValueInfo
			fmt.Println("x=", x)
			x = strings.ToUpper(x)
			ctx.Task.Variables["x"] = camunda_client_go.Variable{Value: x, Type: t, ValueInfo: i}
			//ctx.HandleFailure()
			ctx.Complete(processor.QueryComplete{})
			return nil
		},
	)
}
