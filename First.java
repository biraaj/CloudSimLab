package cloudsim_test1;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
public class First {
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, int vms, int idShift) {
		LinkedList<Vm> list = new LinkedList<Vm>();

		long size = 10000;
		int ram = 512;
		int mips = 250;
		long bw = 1000;
		int pesNumber = 1;
		String vmm = "Xen";
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[i]);
		}

		return list;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		long length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
						cloudlet[i].setUserId(userId);
						list.add(cloudlet[i]);
					}

					return list;
				}


				public static void main(String[] args) {
					Log.printLine("Starting Clouddemo...");

					try {
						int num_user = 2;
						Calendar calendar = Calendar.getInstance();
						boolean trace_flag = false;
						CloudSim.init(num_user, calendar, trace_flag);

						@SuppressWarnings("unused")
						Datacenter datacenter0 = createDatacenter("Datacenter_0");
						
						DatacenterBroker broker = createBroker("Broker_0");
						int brokerId = broker.getId();

						vmlist = createVM(brokerId, 2, 0);
						cloudletList = createCloudlet(brokerId, 8, 0);

						broker.submitVmList(vmlist);
						broker.submitCloudletList(cloudletList);

						CloudSim.startSimulation();

						List<Cloudlet> newList = broker.getCloudletReceivedList();

						CloudSim.stopSimulation();

						printCloudletList(newList);

						Log.printLine("Clouddemo finished!");
					}
					catch (Exception e)
					{
						e.printStackTrace();
						Log.printLine("The simulation has been terminated due to an unexpected error");
					}
				}

				private static Datacenter createDatacenter(String name){

					List<Host> hostList = new ArrayList<Host>();

					List<Pe> peList1 = new ArrayList<Pe>();

					int mips = 1000;

					peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
					peList1.add(new Pe(1, new PeProvisionerSimple(mips)));

					int hostId=0;
					int ram = 16384;
					long storage = 1000000;
					int bw = 10000;

					hostList.add(
			    			new Host(
			    				hostId,
			    				new RamProvisionerSimple(ram),
			    				new BwProvisionerSimple(bw),
			    				storage,
			    				peList1,
			    				new VmSchedulerSpaceShared(peList1)
			    			)
			    		); 

					String arch = "x86";
					String os = "Linux";
					String vmm = "Xen";
					double time_zone = 10.0; 
					double cost = 3.0;
					double costPerMem = 0.05;
					double costPerStorage = 0.1;
					double costPerBw = 0.1;
					LinkedList<Storage> storageList = new LinkedList<Storage>();

					DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
			                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


					Datacenter datacenter = null;
					try {
						datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
					} catch (Exception e) {
						e.printStackTrace();
					}

					return datacenter;
				}
				private static DatacenterBroker createBroker(String name){

					DatacenterBroker broker = null;
					try {
						broker = new DatacenterBroker(name);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
					return broker;
				}

				private static void printCloudletList(List<Cloudlet> list) {
					int size = list.size();
					Cloudlet cloudlet;

					String indent = "    ";
					Log.printLine();
					Log.printLine("========== OUTPUT ==========");
					Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
							"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

					DecimalFormat dft = new DecimalFormat("###.##");
					for (int i = 0; i < size; i++) {
						cloudlet = list.get(i);
						Log.print(indent + cloudlet.getCloudletId() + indent + indent);
					if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
							Log.print("SUCCESS");
							Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
									indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
									indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
					}
					}
				}
			}

