package mveritym.cashflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nijikokun.cashflowregister.payment.Method.MethodAccount;

public class SalaryManager {
	
	protected static CashFlow cashFlow;
    protected static Configuration conf;
    protected static Configuration uconf;
    protected File confFile;
    List<String> salaries;
    List<String> paidGroups;
    ListIterator<String> iterator;
    Timer timer = new Timer();
    Collection<Taxer> salaryTasks = new ArrayList<Taxer>();

	public SalaryManager(CashFlow cashFlow) {
		conf = null;
		loadConf();
		SalaryManager.cashFlow = cashFlow;
		salaries = conf.getStringList("salaries.list", null);
	}
	
	public void loadConf() {
		File f = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");

        if (f.exists()) {
        	conf = new Configuration(f);
        	conf.load();
        }
        else {
        	System.out.println("[CashFlow] No config file found. Creating data file.");
        	this.confFile = new File(TaxManager.cashFlow.getDataFolder(), "config.yml");
            TaxManager.conf = new Configuration(confFile);  
            List<String> tempList = null;
            conf.setProperty("salaries.list", tempList);
            conf.save();
        }
    }
	
	public void createSalary(CommandSender sender, String name, String paycheck, String interval, String employer) {
		String salaryName = name;
		double salary = Double.parseDouble(paycheck);
		double salaryInterval = Double.parseDouble(interval);
		List<String> paidGroups = null;
		
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		iterator = salaries.listIterator();
		
		if(salary <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a salary greater than 0.");
			return;
		} else if(salaryInterval <= 0) {
			sender.sendMessage(ChatColor.RED + "Please choose a salary interval greater than 0.");
			return;
		} else if(!SalaryManager.cashFlow.isPlayer(employer) && !(employer.equals("null"))) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return;
		} else {
			while(iterator.hasNext()) {
				if(iterator.next().equals(salaryName)) {
					sender.sendMessage(ChatColor.RED + "A salary with that name has already been created.");
					return;
				}
			}
		}
		
		salaries.add(salaryName);	
		conf.setProperty("salaries.list", salaries);
		conf.setProperty("salaries." + salaryName + ".salary", salary);
		conf.setProperty("salaries." + salaryName + ".salaryInterval", salaryInterval);
		conf.setProperty("salaries." + salaryName + ".employer", employer);
		conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
		conf.setProperty("salaries." + salaryName + ".lastPaid", null);
		conf.setProperty("salaries." + salaryName + ".exceptedPlayers", null);
		conf.save();
	
		sender.sendMessage(ChatColor.GREEN + "New salary " + salaryName + " created successfully.");
	}
	
	public void deleteSalary(CommandSender sender, String name) {
		String salaryName = name;
		
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		
		if(salaries.contains(salaryName)) {
			salaries.remove(salaryName);
			conf.setProperty("salaries.list", salaries);
			conf.removeProperty("salaries." + salaryName);
			conf.save();
			
			sender.sendMessage(ChatColor.GREEN + "Salary " + salaryName + " deleted successfully.");
		} else {
			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName);
		}
		
		return;
	}
	
	public void salaryInfo(CommandSender sender, String salaryName) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		
		if(salaries.contains(salaryName)) {
			sender.sendMessage(ChatColor.BLUE + "Salary: " + conf.getString("salaries." + salaryName + ".salary"));
			sender.sendMessage(ChatColor.BLUE + "Interval: " + conf.getString("salaries." + salaryName + ".salaryInterval") + " hours");
			sender.sendMessage(ChatColor.BLUE + "Receiving player: " + conf.getString("salaries." + salaryName + ".employer"));
			sender.sendMessage(ChatColor.BLUE + "Paying groups: " + conf.getStringList("salaries." + salaryName + ".paidGroups", null));
			sender.sendMessage(ChatColor.BLUE + "Excepted users: " + conf.getStringList("salaries." + salaryName + ".exceptedPlayers", null));
		} else {
			sender.sendMessage(ChatColor.RED + "No salary, " + salaryName + ", found.");
		}
		
		return;
	}
	
	public void listSalaries(CommandSender sender) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		iterator = salaries.listIterator();
		
		if(salaries.size() != 0) {
			while(iterator.hasNext()) {
				sender.sendMessage(ChatColor.BLUE + iterator.next());
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No salaries to list.");
		}
	}
	
	public void applySalary(CommandSender sender, String salaryName, String groupName) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		paidGroups = conf.getStringList("salaries." + salaryName + ".paidGroups", null);
		
		if(!(salaries.contains(salaryName))) {
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		}
		/*
		else if(!(SalaryManager.cashFlow.permsManager.isGroup(groupName))){
			sender.sendMessage(ChatColor.RED + "Group not found.");
		}
		*/ 
		else {
			sender.sendMessage(ChatColor.GREEN + salaryName + " applied successfully to " + groupName);
			paidGroups.add(groupName);
			conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
			conf.save();
		}
		
		return;
	}
	
	public void removeSalary(CommandSender sender, String salaryName, String groupName) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		paidGroups = conf.getStringList("taxes." + salaryName + ".payingGroups", null);
		
		if(!(salaries.contains(salaryName))) {
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		} else if(!(paidGroups.contains(groupName))) {
			sender.sendMessage(ChatColor.RED + "Group not found.");
		} else {
			sender.sendMessage(ChatColor.GREEN + salaryName + " removed successfully from " + groupName);
			paidGroups.remove(groupName);
			conf.setProperty("salaries." + salaryName + ".paidGroups", paidGroups);
			conf.save();
		}
		
		return;
	}
	
	public void enable() {		
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		Double hours;
		Date lastPaid;
		
		for(String salaryName : salaries) {
			hours = Double.parseDouble(conf.getString("salaries." + salaryName + ".salaryInterval"));
			lastPaid = (Date) conf.getProperty("salaries." + salaryName + ".lastPaid");
			System.out.println(conf.getProperty("salaries." + salaryName + ".lastPaid"));
			Taxer taxer = new Taxer(this, salaryName, hours, lastPaid);
			salaryTasks.add(taxer);
		}
	}
	
	public void disable() {
		for(Taxer salaryTask : salaryTasks) {
			salaryTask.cancel();
		}
	}
	
	public void addException(CommandSender sender, String salaryName, String userName) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		List<String> exceptedPlayers = conf.getStringList("salaries." + salaryName + ".exceptedPlayers", null);
		
		if(!(salaries.contains(salaryName))) {
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		} else if(salaries.contains(userName)) {
			sender.sendMessage(ChatColor.RED + userName + " is already listed as excepted.");
		} else {
			sender.sendMessage(ChatColor.GREEN + userName + " added as an exception.");
			exceptedPlayers.add(userName);
			conf.setProperty("salaries." + salaryName + ".exceptedPlayers", exceptedPlayers);
			conf.save();
		}
		
		return;
	}
	
	public void removeException(CommandSender sender, String salaryName, String userName) {
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		List<String> exceptedPlayers = conf.getStringList("salaries." + salaryName + ".exceptedPlayers", null);
		
		if(!(salaries.contains(salaryName))) {
			sender.sendMessage(ChatColor.RED + "Salary not found.");
		} else if(!(exceptedPlayers.contains(userName))) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
		} else {
			sender.sendMessage(ChatColor.GREEN + userName + " removed as an exception.");
			exceptedPlayers.remove(userName);
			conf.setProperty("salaries." + salaryName + ".exceptedPlayers", exceptedPlayers);
			conf.save();
		}
	}
	
	public void paySalary(String salaryName) {
		System.out.println("Paying salary " + salaryName);
		
		loadConf();
		salaries = conf.getStringList("salaries.list", null);
		
		conf.setProperty("salaries." + salaryName + ".lastPaid", new Date());
		conf.save();
		
		List<String> groups = conf.getStringList("salaries." + salaryName + ".paidGroups", null);
		List<String> exceptedPlayers = conf.getStringList("salaries." + salaryName + ".exceptedPlayers", null);
		Double salary = Double.parseDouble(conf.getString("salaries." + salaryName + ".salary"));
		String employer = conf.getString("salaries." + salaryName + ".employer");
		
		List<String> users = SalaryManager.cashFlow.permsManager.getUsers(groups, exceptedPlayers);
		for(String user : users) {
			if(SalaryManager.cashFlow.Method.hasAccount(user)) {
				MethodAccount userAccount = SalaryManager.cashFlow.Method.getAccount(user);
				userAccount.add(salary);
				Player player = SalaryManager.cashFlow.getServer().getPlayer(user);
				if(player != null) {
					String message = "You have received $" + salary + " for your salary";
					if(employer.equals("null")) {
						message += ".";
					} else {
						message += " from " + employer + ".";
					}
					player.sendMessage(ChatColor.BLUE + message);
				}
				
				if(!(employer.equals("null"))) {
					MethodAccount employerAccount = SalaryManager.cashFlow.Method.getAccount(employer);
					employerAccount.subtract(salary);
				}
			}
		}
	}
}
