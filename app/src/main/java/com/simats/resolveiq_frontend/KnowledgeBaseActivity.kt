package com.simats.resolveiq_frontend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.resolveiq_frontend.adapter.KnowledgeArticleAdapter
import com.simats.resolveiq_frontend.data.model.KnowledgeArticle
import com.simats.resolveiq_frontend.databinding.ActivityKnowledgeBaseBinding

class KnowledgeBaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKnowledgeBaseBinding
    private lateinit var adapter: KnowledgeArticleAdapter
    private var allArticles: List<KnowledgeArticle> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        loadMockData()
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = KnowledgeArticleAdapter(emptyList()) { article ->
            val intent = android.content.Intent(this, KnowledgeDetailActivity::class.java).apply {
                putExtra("article", article)
            }
            startActivity(intent)
        }
        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        binding.rvArticles.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            filterArticles(text.toString(), getSelectedCategory())
        }
    }

    private fun setupFilters() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            filterArticles(binding.etSearch.text.toString(), getSelectedCategory())
        }
    }

    private fun getSelectedCategory(): String {
        return when (binding.chipGroup.checkedChipId) {
            R.id.chipNetwork -> "Network"
            R.id.chipHardware -> "Hardware"
            R.id.chipSoftware -> "Software"
            R.id.chipApplication -> "Application"
            R.id.chipGeneral -> "General"
            else -> "All"
        }
    }

    private fun loadMockData() {
        allArticles = listOf(
            // --- NETWORK ---
            KnowledgeArticle(1, "Troubleshooting Enterprise WiFi Connectivity", "Steps to resolve frequent disconnects and low signal issues in office environments.", 
                "1. Verify the device is within range of the WAP.\n2. Check if the SSID is visible; if not, restart the wireless adapter.\n3. Forget the network and rejoin using corporate credentials.\n4. If issues persist, update the network drivers.\n5. Contact IT for VLAN verification.", "Network", "Oct 12, 2023"),
            KnowledgeArticle(2, "VPN Tunnel Failure Resolution", "Professional guide to fixing VPN connection errors for remote employees.", 
                "1. Ensure the user has an active internet connection.\n2. Verify the VPN gateway address is correct.\n3. Update the VPN client to the latest version.\n4. Clear the DNS cache using 'ipconfig /flushdns'.\n5. Check if the user's account is locked in Active Directory.", "Network", "Oct 15, 2023"),
            KnowledgeArticle(3, "Static IP Configuration for Servers", "Procedure for assigning static IP addresses in the production environment.", 
                "1. Access the Network and Sharing Center.\n2. Navigate to IPv4 properties.\n3. Input the assigned IP, Subnet Mask, and Gateway.\n4. Set Primary and Secondary DNS as provided by the Infrastructure team.\n5. Test connectivity with 'ping' command.", "Network", "Oct 20, 2023"),
            KnowledgeArticle(4, "Firewall Port Request Process", "How to request specific port openings for internal applications.", 
                "1. Identify the source and destination IP addresses.\n2. Determine the required port numbers and protocols (TCP/UDP).\n3. Submit a ticket via the Security portal.\n4. Provide a business justification for the request.\n5. Wait for the Security team's validation and implementation.", "Network", "Oct 22, 2023"),
            KnowledgeArticle(5, "Switch Port Diagnostics", "Identifying and fixing faulty physical network connections.", 
                "1. Verify the link light status on both the NIC and Switch.\n2. Replace the Ethernet cable with a certified Cat6 cable.\n3. Test the wall jack with a known working device.\n4. Check the port status on the switch management console.\n5. Re-patch the connection if the port is found defective.", "Network", "Oct 25, 2023"),

            // --- HARDWARE ---
            KnowledgeArticle(6, "Laptop Battery Health Maintenance", "Guidelines for extending the life of corporate laptop batteries.", 
                "1. Avoid keeping the laptop plugged in 24/7.\n2. Keep the BIOS updated to the latest version.\n3. Run a battery calibration once every 3 months.\n4. Use only the original manufacturer-provided charger.\n5. Report any signs of swelling to Hardware Support immediately.", "Hardware", "Nov 01, 2023"),
            KnowledgeArticle(7, "Docking Station Troubleshooting", "Resolving display and peripheral issues with universal docks.", 
                "1. Disconnect and reconnect the USB-C/Thunderbolt cable.\n2. Ensure the dock's firmware is up to date.\n3. Verify the video connectors (DP/HDMI) are securely seated.\n4. Power cycle the dock by unplugging it for 30 seconds.\n5. Test with a different laptop to isolate the dock as the cause.", "Hardware", "Nov 03, 2023"),
            KnowledgeArticle(8, "Printer Jam Clearance Protocol", "Professional steps for clearing jams in multi-function printers.", 
                "1. Follow the on-screen prompts on the printer display.\n2. Open all accessible doors and trays indicated.\n3. Pull paper out in the direction of the paper path to avoid tearing.\n4. Clean the rollers with a lint-free cloth if necessary.\n5. Restart the printer to clear any sensor errors.", "Hardware", "Nov 05, 2023"),
            KnowledgeArticle(9, "RAM Replacement Procedure", "Standard steps for upgrading or replacing memory modules.", 
                "1. Power down the device and disconnect all power sources.\n2. Ground yourself using an ESD strap.\n3. Remove the chassis cover carefully.\n4. Disengage the retention clips to remove the old RAM.\n5. Insert the new module at a 45-degree angle and press down firmly.", "Hardware", "Nov 08, 2023"),
            KnowledgeArticle(10, "External Monitor Not Detected", "Fixing display output issues on workstations.", 
                "1. Check the monitor power and input source selection.\n2. Ensure the display cable (HDMI/DP) is fully connected.\n3. Use 'Win+P' to cycle through display modes (Extend/Duplicate).\n4. Update the Integrated Graphics and Dedicated GPU drivers.\n5. Test with a different cable or adapter.", "Hardware", "Nov 10, 2023"),

            // --- SOFTWARE ---
            KnowledgeArticle(11, "Standard Software Stack Installation", "Automated deployment guide for new employee workstations.", 
                "1. Join the machine to the corporate domain.\n2. Launch the Software Center application.\n3. Select 'Base Image Essentials' bundle.\n4. Click 'Install All' and wait for completion.\n5. Restart the system to finalize registry changes.", "Software", "Nov 12, 2023"),
            KnowledgeArticle(12, "Browser Cache and Cookie Management", "Standardizing browser troubleshooting across the organization.", 
                "1. Open Settings and navigate to 'Privacy and Security'.\n2. Select 'Clear browsing data'.\n3. Choose 'All time' for the time range.\n4. Check Cookies and Cached images specifically.\n5. Click 'Clear data' and restart the browser.", "Software", "Nov 15, 2023"),
            KnowledgeArticle(13, "Anti-Virus Signature Updates", "Ensuring systems are protected against the latest threats.", 
                "1. Locate the Endpoint Security icon in the system tray.\n2. Right-click and select 'Check for Updates'.\n3. Verify the signature date is current.\n4. If update fails, check connectivity to the management server.\n5. Run a full scan once updates are complete.", "Software", "Nov 18, 2023"),
            KnowledgeArticle(14, "Office 365 Activation Errors", "Resolving licensing issues for productivity software.", 
                "1. Sign out of all Office applications.\n2. Run the 'SARA' tool (Support and Recovery Assistant).\n3. Clear stored credentials in Windows Credential Manager.\n4. Sign back in with the corporate O365 account.\n5. Verify subscription status in the portal.", "Software", "Nov 20, 2023"),
            KnowledgeArticle(15, "PDF Editor License Transfer", "Moving premium software licenses between users.", 
                "1. Deactivate the license on the old device.\n2. Uninstall the software from the source machine.\n3. Record the license key for the destination user.\n4. Install the software on the new device.\n5. Enter the license key and complete online activation.", "Software", "Nov 22, 2023"),

            // --- APPLICATION ---
            KnowledgeArticle(16, "ERP Portal Login Troubleshooting", "Fixing access issues for the core enterprise application.", 
                "1. Ensure the user is using the internal link or VPN.\n2. Check if the user's account is active in the ERP database.\n3. Clear browser cookies specifically for the ERP domain.\n4. Verify JavaScript and pop-ups are allowed for the site.\n5. Escalate to the DevOps team if the server returns a 500 error.", "Application", "Dec 01, 2023"),
            KnowledgeArticle(17, "CRM Dashboard Synchronization", "Resolving data lag in the customer management system.", 
                "1. Click the 'Sync' button in the CRM settings.\n2. Check for missing required fields in recent records.\n3. Verify API connectivity to the backend services.\n4. Clear the local application cache.\n5. Re-login to trigger a full metadata refresh.", "Application", "Dec 03, 2023"),
            KnowledgeArticle(18, "Human Resources System Access", "Assisting employees with specialized HR portal issues.", 
                "1. Direct the user to use the Single Sign-On (SSO) option.\n2. Verify the Employee ID is correctly mapped in the HR system.\n3. Ensure the browser version is compatible with the portal.\n4. Check if the user has completed the mandatory onboarding tasks.\n5. Contact HR Systems admin for permission audits.", "Application", "Dec 05, 2023"),
            KnowledgeArticle(19, "Reporting Tool Export Failures", "Fixing issues when extracting large datasets.", 
                "1. Reduce the date range or filter criteria to limit data size.\n2. Choose 'CSV' instead of 'Excel' for faster processing.\n3. Ensure the browser popup blocker is disabled.\n4. Check available disk space on the local machine.\n5. Schedule the report during off-peak hours.", "Application", "Dec 08, 2023"),
            KnowledgeArticle(20, "Internal Communication Tool Reset", "Resolving 'Message Sync' errors in the corporate chat app.", 
                "1. Sign out and quit the application completely.\n2. Delete the application's local AppData folder.\n3. Re-install the latest version from the app portal.\n4. Sign in and wait for the initial sync to complete.\n5. Check notification settings in both app and OS.", "Application", "Dec 10, 2023"),

            // --- GENERAL ---
            KnowledgeArticle(21, "New Employee IT Onboarding", "Checklist for setting up technical assets for new hires.", 
                "1. Provision Active Directory and Email accounts.\n2. Assign necessary security groups and distribution lists.\n3. Prepare the hardware kit (Laptop, Monitor, Peripherals).\n4. Schedule a 30-minute IT orientation session.\n5. Hand over the 'Welcome Guide' with temporary credentials.", "General", "Jan 05, 2024"),
            KnowledgeArticle(22, "Asset Return Protocol", "Process for recovering equipment from departing employees.", 
                "1. Inventory all returned items against the original assignment.\n2. Inspect for physical damage or missing components.\n3. Wipe all data using a DOD-certified secure erase tool.\n4. Update the Asset Management database status to 'In Stock'.\n5. Provide a return receipt to the employee or HR.", "General", "Jan 10, 2024"),
            KnowledgeArticle(23, "IT Support Escalation Matrix", "Guidelines for routing tickets to specialized teams.", 
                "1. Level 1: Initial troubleshooting and basic requests.\n2. Level 2: Complex technical issues requiring remote access.\n3. Level 3: Infrastructure, Network, and Server-side problems.\n4. Critical: Major outages affecting 10+ users.\n5. Use the specific Slack channels for urgent developer support.", "General", "Jan 15, 2024"),
            KnowledgeArticle(24, "Password Policy Overview", "Official rules for maintaining corporate account security.", 
                "1. Minimum 12 characters with multi-factor complexity.\n2. Passwords must be changed every 90 days.\n3. Previous 5 passwords cannot be reused.\n4. Account lock-out occurs after 5 failed attempts.\n5. Do not share credentials or write them down.", "General", "Jan 20, 2024"),
            KnowledgeArticle(25, "Data Backup and Retention Rules", "Corporate policy for safeguarding business information.", 
                "1. Use OneDrive for all individual business documents.\n2. Personal data (photos/videos) should not be stored on corporate assets.\n3. Deleted items are retained in the Recycle Bin for 30 days.\n4. Departmental shared drives are backed up nightly.\n5. Regulatory data is archived for a minimum of 7 years.", "General", "Jan 25, 2024")
        )
        adapter.updateArticles(allArticles)
    }

    private fun filterArticles(query: String, category: String) {
        val filtered = allArticles.filter { article ->
            val matchesQuery = article.title.contains(query, ignoreCase = true) || 
                               article.summary.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || article.category == category
            matchesQuery && matchesCategory
        }
        adapter.updateArticles(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
