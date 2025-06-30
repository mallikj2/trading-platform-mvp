# Real-time Algorithmic Trading Platform - MVP

This project is the Minimum Viable Product (MVP) for a real-time, open-source algorithmic trading platform. The MVP focuses on establishing the core backend components for stock data ingestion and basic technical analysis.

## Project Goals (MVP Scope)

*   **Simulated Stock Data Ingestion:** Load hardcoded historical stock data into an in-memory database.
*   **Technical Analysis Engine:** Implement a basic technical indicator (Simple Moving Average - SMA) using the TA4j library.
*   **REST Endpoints:** Expose API endpoints to retrieve stock data and calculate SMA.
*   **Data Storage:** Utilize an in-memory H2 database for quick setup and development.

## Technologies Used

*   **Spring Boot:** Framework for building stand-alone, production-grade Spring applications.
*   **Java 17+:** The primary programming language.
*   **Spring Data JPA:** For data persistence with H2.
*   **H2 Database:** An in-memory relational database for development and testing.
*   **TA4j:** An open-source Java library for technical analysis.
*   **Lombok:** To reduce boilerplate code (e.g., getters, setters).
*   **Maven:** Dependency management and build automation tool.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven (if not using the wrapper)

### Building the Application

Navigate to the `trading-platform-mvp` directory in your terminal and run the following command:

```bash
# On Linux/macOS
./mvnw clean install

# On Windows
./mvnw.cmd clean install
```

### Running the Application

After building, you can run the application from the `trading-platform-mvp` directory:

```bash
java -jar target/trading-platform-mvp-0.0.1-SNAPSHOT.jar
```

The application will start on port `8080` by default.

## API Documentation

The MVP exposes REST endpoints for interacting with the simulated stock data and technical analysis.

**Base URL:** `http://localhost:8080/api/v1/stock`

### 1. Get Stock Data for a Symbol

*   **Endpoint:** `GET /api/v1/stock/{symbol}`
*   **Description:** Retrieves historical stock data for a given symbol.
*   **Example `curl` Command (for AAPL):**
    ```bash
    curl http://localhost:8080/api/v1/stock/AAPL
    ```
*   **Sample Response (JSON):**
    ```json
    [
        {
            "id": 1,
            "symbol": "AAPL",
            "timestamp": "2024-01-01T09:30:00",
            "open": 170.0,
            "high": 171.0,
            "low": 169.0,
            "close": 170.5,
            "volume": 100000
        },
        {
            "id": 2,
            "symbol": "AAPL",
            "timestamp": "2024-01-01T09:31:00",
            "open": 170.5,
            "high": 171.5,
            "low": 170.0,
            "close": 171.2,
            "volume": 120000
        }
        // ... more data
    ]
    ```

### 2. Calculate Simple Moving Average (SMA) for a Symbol

*   **Endpoint:** `GET /api/v1/stock/{symbol}/sma/{barCount}`
*   **Description:** Calculates the Simple Moving Average (SMA) for the given symbol over a specified number of bars (data points).
*   **Example `curl` Command (for AAPL, 3-bar SMA):**
    ```bash
    curl http://localhost:8080/api/v1/stock/AAPL/sma/3
    ```
*   **Sample Response (Text):**
    ```
    SMA for AAPL over 3 bars: 172.37
    ```
    *(Note: The exact value depends on the hardcoded data and `barCount`.)*

### H2 Database Console

You can access the H2 database console to inspect the data directly.

*   **URL:** `http://localhost:8080/h2-console`
*   **JDBC URL:** `jdbc:h2:mem:testdb` (default)
*   **Username:** `sa` (default)
*   **Password:** (empty by default)

Click "Connect" to access the database.

## Next Steps (Future Enhancements)

This MVP lays the groundwork. Future enhancements will include:

*   **Real-time Data Ingestion:** Integration with external financial data APIs (e.g., Alpha Vantage, Finnhub).
*   **Advanced Technical Indicators:** Implementing a wider range of TA4j indicators.
*   **Strategy Definition & Management:** Developing logic for defining and applying trading strategies.
*   **Signal Generation & Management:** Automating buy/sell signal generation and handling.
*   **Backtesting Engine:** Building a robust backtesting capability to evaluate strategies against historical data.
*   **User Interface:** A web-based frontend for visualization and interaction.
*   **Messaging Queue:** Integration with Apache Kafka for real-time data streaming between microservices.
*   **Python Integration:** Exploring machine learning models for signal generation.
