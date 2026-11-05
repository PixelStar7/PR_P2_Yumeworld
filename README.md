# 🎮 YumeWorld

Projecte 2 del CFGS de Desenvolupament d'Aplicacions Multiplataforma.

![Made with Love](https://img.shields.io/badge/Made%20with-Love-pink?style=for-the-badge&logo=heart)
![Status](https://img.shields.io/badge/Status-Completat-brightgreen?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-brightgreen?style=for-the-badge&logo=springboot)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![.NET](https://img.shields.io/badge/.NET-8-512BD4?style=for-the-badge&logo=dotnet)
![MariaDB](https://img.shields.io/badge/MariaDB-10.4-003545?style=for-the-badge&logo=mariadb)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)

## 📝 Descripció

**YumeWorld** és una plataforma completa per a un videojoc multijugador basat en torns i exploració de masmorres. El projecte combina:

* **Servidor de Joc en Temps Real (`ym_server`)**: Desenvolupat amb **Java 17**, **Spring Boot** i **WebSockets**, gestiona la lògica de partides multijugador, instàncies de sales, combats per torns, esdeveniments de descans i cofres, i persistència de dades mitjançant JPA/Hibernate.
* **Gestor i Editor de Contingut (`ym_editor`)**: Aplicació d'escriptori desenvolupada en **WPF** i **.NET 8** connectada a la capa de dades (`ym_model`) per gestionar el contingut del món:
  * 🧑 Personatges (Yumedants i jugadors)
  * ⚔️ Enemics, biomes i recompenses
  * ⚡ Habilitats, efectes i estats alterats
  * 🗺️ Actes i generació de sales
* **Infraestructura Contenidoritzada (`docker`)**: Entorn automatitzat amb Docker Compose per desplegar la base de dades MariaDB amb dades inicials, phpMyAdmin i el servidor de joc.


## 📂 Estructura del Projecte

```text
PR_P2_Yumeworld/
├── ym_docker/                 # Configuració de Docker Compose i inicialització SQL
│   ├── init/               # Scripts de creació de taules (schema) i dades inicials
│   └── docker-compose.yml  # Orquestració de serveis (BD, phpMyAdmin, Spring Server)
├── docs/                   # Documentació tècnica, diagrames d'estats i presentació
├── resources/              # Sprites, icones d'estats, fons i recursos multimèdia
├── ym_editor/              # Projecte WPF (.NET 8) per a l'editor de dades
├── ym_model/               # Biblioteca de classes C# / Entity Framework per al model
└── ym_server/              # Backend Java 17 amb Spring Boot i WebSockets


## 🛠️ Requisits previs

* **Docker & Docker Desktop** (recomanat per aixecar servidor i base de dades sense instal·lacions manuals).
* **.NET 8 SDK** i **Visual Studio 2022** (amb suport per a desenvolupament d'escriptori .NET per a `ym_editor`).
* **Java 17 JDK** (només si es vol compilar i executar `ym_server` directament en local sense Docker).

## 📦 Com Instal·lar

1. Clona el repositori:
```bash
git clone https://github.com/[usuari]/PR_P2_Yumeworld.git
cd PR_P2_Yumeworld
```

2. Copiar les variables d'entorn:
```bash
cp .env.example docker/.env
```

3. Aixecar els serveis:
```bash
cd docker
docker compose up -d --build
```



## 🙌 Col·laboradors

| Nom | Rol | Contacte |
|-----|-----|----------|
| **Pau Tort Balsach** | Desenvolupador | [LinkedIn](https://www.linkedin.com/in/pautortbalsach) \| [GitHub](https://github.com/PixelStar7) |
| **Sara Prats Morales** | Desenvolupadora | [LinkedIn](https://www.linkedin.com/in/sara-prats-morales) \| [GitHub](https://github.com/kuro-spm) |

Institut Milà i Fontanals — Curs 2025/2026

## 📖 Documentació

A la carpeta docs/ trobaràs:

- Diagrames d'arquitectura i d'estats: estats_client.png i estats_servidor.png.
- Model de dades: Projecte Enterprise Architect Yumeworld.EAP i scripts DDL/Data.
- Presentació del projecte: YumeWorld_Presentacio.pptx i captures de les pantalles.

## 💡 Contacte

Si tens preguntes o suggeriments, no dubtis en obrir una issue o contactar amb els autors.

---

**Última actualització:** Novembre 2026
