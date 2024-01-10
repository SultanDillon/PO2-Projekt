-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sty 10, 2024 at 05:43 PM
-- Wersja serwera: 10.4.32-MariaDB
-- Wersja PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wypozyczalnia_dvd`
--

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `biblioteka`
--

CREATE TABLE `biblioteka` (
  `ID` int(11) NOT NULL,
  `Kategoria` varchar(20) NOT NULL,
  `Nazwa` varchar(30) NOT NULL,
  `Opis` varchar(100) NOT NULL,
  `Rok Produkcji` int(4) NOT NULL,
  `Kategoria Wiekowa` int(2) NOT NULL,
  `Ilosc` int(4) NOT NULL,
  `Cena` int(9) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `klient`
--

CREATE TABLE `klient` (
  `ID` int(11) NOT NULL,
  `Imie` varchar(11) NOT NULL,
  `Nazwisko` varchar(11) NOT NULL,
  `Miasto` varchar(11) NOT NULL,
  `Ulica` varchar(11) NOT NULL,
  `Nr_domu/lokalu` int(11) NOT NULL,
  `Kod_Pocztowy` int(11) NOT NULL,
  `Telefon` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `pracownik`
--

CREATE TABLE `pracownik` (
  `ID` int(11) NOT NULL,
  `Imie` varchar(11) NOT NULL,
  `Nazwisko` varchar(11) NOT NULL,
  `Miasto` varchar(11) NOT NULL,
  `Ulica` varchar(11) NOT NULL,
  `Nr_domu/lokalu` int(11) NOT NULL,
  `Kod_Pocztowy` int(11) NOT NULL,
  `Telefon` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `rezerwacje`
--

CREATE TABLE `rezerwacje` (
  `ID` int(11) NOT NULL,
  `ID_Klient` int(11) NOT NULL,
  `ID_Filmu` int(11) NOT NULL,
  `Data_pocz_rezerwacji` date NOT NULL,
  `Data_kon_rezerwacji` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `wypozyczenia`
--

CREATE TABLE `wypozyczenia` (
  `ID` int(11) NOT NULL,
  `ID_Przedmiotu` int(11) NOT NULL,
  `ID_Klienta` int(11) NOT NULL,
  `ID_Pracownika` int(11) NOT NULL,
  `DATA_Wyp` date NOT NULL,
  `DATA_Zwr` date NOT NULL,
  `Cena` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_polish_ci;

--
-- Indeksy dla zrzutów tabel
--

--
-- Indeksy dla tabeli `biblioteka`
--
ALTER TABLE `biblioteka`
  ADD PRIMARY KEY (`ID`);

--
-- Indeksy dla tabeli `klient`
--
ALTER TABLE `klient`
  ADD PRIMARY KEY (`ID`);

--
-- Indeksy dla tabeli `pracownik`
--
ALTER TABLE `pracownik`
  ADD PRIMARY KEY (`ID`);

--
-- Indeksy dla tabeli `rezerwacje`
--
ALTER TABLE `rezerwacje`
  ADD KEY `ID_Filmu` (`ID_Filmu`),
  ADD KEY `ID_Klient` (`ID_Klient`);

--
-- Indeksy dla tabeli `wypozyczenia`
--
ALTER TABLE `wypozyczenia`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `ID_Klienta` (`ID_Klienta`),
  ADD KEY `ID_Pracownika` (`ID_Pracownika`),
  ADD KEY `ID_Przedmiotu` (`ID_Przedmiotu`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `rezerwacje`
--
ALTER TABLE `rezerwacje`
  ADD CONSTRAINT `rezerwacje_ibfk_1` FOREIGN KEY (`ID_Filmu`) REFERENCES `biblioteka` (`ID`),
  ADD CONSTRAINT `rezerwacje_ibfk_2` FOREIGN KEY (`ID_Klient`) REFERENCES `klient` (`ID`);

--
-- Constraints for table `wypozyczenia`
--
ALTER TABLE `wypozyczenia`
  ADD CONSTRAINT `wypozyczenia_ibfk_1` FOREIGN KEY (`ID_Klienta`) REFERENCES `klient` (`ID`),
  ADD CONSTRAINT `wypozyczenia_ibfk_2` FOREIGN KEY (`ID_Pracownika`) REFERENCES `pracownik` (`ID`),
  ADD CONSTRAINT `wypozyczenia_ibfk_3` FOREIGN KEY (`ID_Przedmiotu`) REFERENCES `biblioteka` (`ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
