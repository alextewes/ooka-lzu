package cli;

import lzu.ComponentLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ComponentAssemblerCLI {
    private static ComponentLoader componentLoader = new ComponentLoader();

    @Command(name = "", description = "JVM Runtime Environment", subcommands = CommandLine.HelpCommand.class)
    static class startRTE implements Callable<Integer> {
        @Override

        public Integer call() {
            componentLoader.startRuntime();
            System.out.println("Runtime started");
            return 0;
        }
    }

    @Command(name = "deploy", description = "Deploy a new component from a JAR file.")
    static class DeployCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The path to the component JAR file.")
        private Path componentJarPath;

        @Parameters(index = "1", description = "The name of the component.")
        private String componentName;

        @Override
        public Integer call() {
            try {
                componentLoader.deployComponent(componentJarPath, componentName);
                System.out.println("Component deployed with name: " + componentName);
            } catch (Exception e) {
                System.err.println("Error deploying component: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "start", description = "Start a component by ID.")
    static class StartCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The ID of the component to start.")
        private int componentId;

        @Override
        public Integer call() {
            try {
                componentLoader.startComponentById(componentId);
                System.out.println("Component started with ID: " + componentId);
            } catch (Exception e) {
                System.err.println("Error starting component: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "stop", description = "Stop a component by ID.")
    static class StopCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The ID of the component to stop.")
        private int componentId;

        @Override
        public Integer call() {
            try {
                componentLoader.stopComponentById(componentId);
                System.out.println("Component stopped with ID: " + componentId);
            } catch (Exception e) {
                System.err.println("Error stopping component: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "status", description = "Get the status of all components.")
    static class StatusCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            try {
                List<Map<String, Object>> statuses = componentLoader.getAllComponentStatuses();
                if (statuses.isEmpty()) {
                    System.out.println("No components deployed.");
                } else {
                    System.out.println("Component statuses:");
                    for (Map<String, Object> status : statuses) {
                        System.out.println(status);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error retrieving component statuses: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "remove", description = "Remove a component by ID.")
    static class RemoveCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The ID of the component to remove.")
        private int componentId;

        @Override
        public Integer call() {
            try {
                componentLoader.removeComponentById(componentId);
                System.out.println("Component removed with ID: " + componentId);
            } catch (Exception e) {
                System.err.println("Error removing component: " + e.getMessage());
                return 1;
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new startRTE())
                .addSubcommand("deploy", new DeployCommand())
                .addSubcommand("start", new StartCommand())
                .addSubcommand("stop", new StopCommand())
                .addSubcommand("status", new StatusCommand())
                .addSubcommand("remove", new RemoveCommand());

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welcome to the Component Assembler CLI!");
        System.out.println("Type 'help' for a list of available commands.");

        while (true) {
            System.out.print("> ");
            String line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                continue;
            }
            if (line == null || line.trim().equalsIgnoreCase("exit")) {
                componentLoader.stopRuntime();
                break;
            }

            String[] commandLineArgs = line.trim().split("\\s+");
            int exitCode = cmd.execute(commandLineArgs);
            if (exitCode != 0) {
                System.out.println("Error: Command execution failed with exit code " + exitCode);
            }
        }
    }
}