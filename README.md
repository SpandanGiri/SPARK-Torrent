
# SPARK-Torrent

SPARK-Torrent is a Java-based BitTorrent client designed for efficient and scalable peer-to-peer file sharing. Utilizing the BitTorrent protocol, it enables users to download and upload files through a distributed network of peers, ensuring faster and more reliable file transfers.

## Getting Started

### Using java IDE

To start the SPARK-Torrent project in java IDE's such as NetBeans/IntelliJ IDEA, follow these steps:

1. **Open the Project:**
   - Clone the repository to your local machine.
     ```bash
     git clone https://github.com/SpandanGiri/SPARK-Torrent.git
     ```
   - Open IDE and select the SPARK-Torrent folder.
   - Navigate to the cloned SPARK-Torrent directory and select the project.

2. **Open Bittorrent.java:**
   - In the Projects view, navigate to `src/main/java/com/spark/bittorrent`.
   - Double-click on `Bittorrent.java` to open the file.

3. **Run the Project:**
   - Once the file is open, run the project from the menu.
   - In the GUI that appears, paste the path of your `.torrent` file and click `Submit` in the BitTorrent Window to start the download.

### Using Command Line

**Prerequisites**
Ensure you have the following installed on your machine:

- Java JDK: Version 8 or higher
- Git: To clone the repository

To start the SPARK-Torrent project using the command line, follow these steps:

1. **Clone the Repository**

Open a terminal or command prompt and run the following command to clone the SPARK-Torrent repository:
```bash
git clone https://github.com/SpandanGiri/SPARK-Torrent.git
```

2. **Navigate to the Project Directory**

Change the directory to the project's `src/main/java` folder:
```bash
cd SPARK-Torrent/src/main/java/com/spark/bittorrent
```

3. **Compile the Java Files**

Use the following command to compile all the Java files:
```bash
javac *.java
```

4. **Run the Application**

Once compiled, you can run the SPARK-Torrent application by executing the main class. Use the following command:
```bash
java com.spark.bittorrent.Main
```

5. **Using the Application**

After running the application, follow the on-screen instructions to load a torrent file and start downloading or seeding content.

## Contributing
We welcome contributions to improve SPARK-Torrent. To contribute:

- Fork the repository.
- Create a new branch for your feature or bugfix.
- Make your changes and commit them.
- Push your changes to your fork.
- Open a Pull Request on the main repository and describe your changes.
