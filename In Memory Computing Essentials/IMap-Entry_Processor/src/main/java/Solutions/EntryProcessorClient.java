package Solutions;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.client.properties.ClientProperty;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;
import hazelcast.Employee;

import static com.hazelcast.client.properties.ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN;
import static com.hazelcast.client.properties.ClientProperty.STATISTICS_ENABLED;

public class EntryProcessorClient {
    public static void main(String[] args) {
        // If you are using the cloud to host your cluster, make sure you add the client credentials!
        //Setting up cloud configuration
        ClientConfig config = new ClientConfig();
        config.setProperty("hazelcast.client.statistics.enabled","true");
        config.setProperty(ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), "YOUR_CLOUD_DISCOVERY_TOKEN");
        config.setClusterName("YOUR_CLUSTER_NAME");



        // Making Employee class available at the Cloud side through User Code Deployment
        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.addClass(hazelcast.Employee.class);
        clientUserCodeDeploymentConfig.addClass(Solutions.EntryProcessorClient.class);
        clientUserCodeDeploymentConfig.setEnabled(true);
        config.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);

        // Create Hazelcast instance which is backed by a client
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);



        // Create a Hazelcast backed map
        IMap<String, Employee> employees = client.getMap("training-ep");

        // Add several Employees with unique keys and different salaries to the map
        employees.put("John", new Employee(20,1000));
        employees.put("Mark", new Employee(30,1000));
        employees.put("Spencer", new Employee(40,1000));
        /**
         * Using EP, increment the salary of each employee by a fixed integer value
         * */
        employees.executeOnEntries((EntryProcessor<String, Employee, Object>) entry -> {
            Employee value = entry.getValue();
            value.incSalary(10);
            entry.setValue(value);
            return null;
        });

        // Read the salaries of all employees to see the change
        for (IMap.Entry<String, Employee> entry : employees.entrySet()) {
            System.out.println(entry.getKey() + " salary: " + entry.getValue().getSalary());
        }

        Hazelcast.shutdownAll();
    }

}