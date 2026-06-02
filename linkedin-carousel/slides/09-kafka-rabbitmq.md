# SLIDE 9 — Messaging design

## Headline
**Kafka vs RabbitMQ — why both?**

| | **Kafka** | **RabbitMQ** |
|---|-----------|--------------|
| Pattern | Event stream · replay | Task queue · ACK |
| VirtuLab | Lab progress → analytics · AI events · audit | Notifications · async jobs |
| Analogy | Recorded announcements | To-do tray |

## n8n automation
- Weekly **RAG reindex**
- DLQ alerts · progress webhooks · circuit-breaker alerts

## Footer
Workflow JSON in `virtulab-n8n/workflows/`
