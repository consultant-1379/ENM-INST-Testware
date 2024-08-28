package com.ericsson.itpf.deployment.test.cases;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.ericsson.nms.host.HostConfigurator;

public class ChangeDefaultPasswordsOnBlades extends TorTestCaseHelper implements TestCase {

    String newLitpAdminPassword = "12shroot";

    String newRootPassword = "12shroot";

    int exitCode;

    String localFileName = "reset_passwords.bsh";

    String localFileName2 = "reset_passwords.py";

    CLICommandHelper cmdHelper;

    String remoteFileLocation = "/tmp/";

    Shell shell;

    User root = new User("root", "12shroot", UserType.ADMIN);

    @TestId(id = "TORF-21627_ChangePasswordsAndVerifyAccessOnBlades_ENMDeployed_Success",
            title = "Change default passwords on all blades deployed and verify access to the blades")
    @Context(context = { Context.CLI })
    @Test
    public void changePasswordsAndVerifyAccessToBlades() {

        Host hostms = HostConfigurator.getMS();

        Host host = HostConfigurator.getDb1();
        accessHost(host, hostms);

        host = HostConfigurator.getDb2();
        accessHost(host, hostms);

        host = HostConfigurator.getSVC1();
        accessHost(host, hostms);

        host = HostConfigurator.getSVC2();
        accessHost(host, hostms);
    }

    public void accessHost(Host host, Host hostms) {
        try {
            setTestInfo("Trying to access " + host.getHostname() + " as litp-admin user");
            cmdHelper = new CLICommandHelper(host);
            cmdHelper.execute("cat /etc/hosts");
            cmdHelper.getStdOut();
            assertEquals("Couldn't access " + host.getHostname() + " as litp-admin user.", 0, cmdHelper.getCommandExitValue());

            setTestInfo("Trying to access " + host.getHostname() + " as root user");
            cmdHelper.newHopBuilder().hop(root).build();
            cmdHelper.execute("cat /etc/hosts");
            cmdHelper.getStdOut();
            exitCode = cmdHelper.getCommandExitValue();
            setTestInfo("Successful login for " + host.getHostname());

        } catch (JSchCLIToolException e) {
            RemoteFileHandler remote = new RemoteFileHandler(hostms);
            remote.copyLocalFileToRemote(localFileName, remoteFileLocation);
            remote.copyLocalFileToRemote(localFileName2, remoteFileLocation);
            setTestInfo("Setting passwords. Please wait ...");

            setPasswords(hostms);

            setTestInfo("Passwords have been set for all blades.");
            try{


            cmdHelper = new CLICommandHelper(host);
            cmdHelper.execute("pwd");
            cmdHelper.getStdOut();
            exitCode = cmdHelper.getCommandExitValue();
            cmdHelper.disconnect();
            }
            catch (JSchCLIToolException ex) {
                setPasswords(hostms);
            }
        }
        assertEquals("The passwords have not been set successfully.", Integer.parseInt("0"), exitCode);
    }

    public void setPasswords(Host hostms) {
        CLI cli = new CLI(hostms);
        shell = cli.executeCommand(Terminal.VT100, "sed -i -e 's/\r//' " + remoteFileLocation + localFileName);
        shell = cli.executeCommand(Terminal.VT100, "sed -i -e 's/\r//' " + remoteFileLocation + localFileName2);
        assertEquals("The script cannot be read", 0, shell.getExitValue());

        String actualCommand = "bash " + remoteFileLocation + localFileName;
        shell = cli.executeCommand(Terminal.VT100, actualCommand);

        while (!shell.isClosed()) {
            shell.read();
        }

        assertEquals("The script didn't execute successfully", 0, shell.getExitValue());
        try {
            Thread.sleep(40000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

}
