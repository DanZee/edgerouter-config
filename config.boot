firewall {
    all-ping enable
    broadcast-ping disable
    ipv6-name WANv6_IN {
        default-action drop
        description "WAN IPv6 naar LAN"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
        rule 30 {
            action accept
            description "Allow IPv6 icmp"
            icmpv6 {
                type echo-request
            }
            protocol ipv6-icmp
        }
    }
    ipv6-name WANv6_LOCAL {
        default-action drop
        description "WAN IPv6 naar Router"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
        rule 30 {
            action accept
            description "Allow IPv6 icmp"
            protocol ipv6-icmp
        }
        rule 40 {
            action accept
            description "Allow dhcpv6"
            destination {
                port 546
            }
            protocol udp
            source {
                port 547
            }
        }
    }
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians enable
    name WAN_IN {
        default-action drop
        description "WAN naar LAN"
        rule 10 {
            action accept
            description "Allow established/related"
            log disable
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    name WAN_LOCAL {
        default-action drop
        description "WAN naar Router"
        rule 10 {
            action accept
            description "Allow established/related"
            log disable
            state {
                established enable
                invalid disable
                new disable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                established disable
                invalid enable
                new disable
                related disable
            }
        }
        rule 30 {
            action accept
            description ike
            destination {
                port 500
            }
            log disable
            protocol udp
        }
        rule 40 {
            action accept
            description esp
            log disable
            protocol esp
        }
        rule 50 {
            action accept
            description nat-t
            destination {
                port 4500
            }
            log disable
            protocol udp
        }
        rule 60 {
            action accept
            description l2tp
            destination {
                port 1701
            }
            ipsec {
                match-ipsec
            }
            log disable
            protocol udp
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        description FTTH
        duplex auto
        mtu 1512
        speed auto
        vif 4 {
            address dhcp
            description "KPN IPTV"
            dhcp-options {
                client-option "send vendor-class-identifier &quot;IPTV_RG&quot;;"
                client-option "request subnet-mask, routers, rfc3442-classless-static-routes;"
                default-route no-update
                default-route-distance 210
                name-server update
            }
            mtu 1500
        }
        vif 6 {
            description "KPN Internet"
            mtu 1508
            pppoe 0 {
                default-route auto
                dhcpv6-pd {
                    no-dns
                    pd 0 {
                        interface eth3 {
                            host-address ::1
                            no-dns
                            prefix-id :1
                            service slaac
                        }
                        prefix-length /48
                    }
                    rapid-commit enable
                }
                firewall {
                    in {
                        ipv6-name WANv6_IN
                        name WAN_IN
                    }
                    local {
                        ipv6-name WANv6_LOCAL
                        name WAN_LOCAL
                    }
                }
                idle-timeout 180
                ipv6 {
                    address {
                        autoconf
                    }
                    dup-addr-detect-transmits 1
                    enable {
                    }
                }
                mtu 1500
                name-server auto
                password ppp
                user-id 18-e8-29-4f-00-12@internet
            }
        }
    }
    ethernet eth2 {
        description "Niet gebruikt"
        disable
    }
    ethernet eth3 {
        address 192.168.2.254/24
        description Thuis
        duplex auto
        ipv6 {
            dup-addr-detect-transmits 1
            router-advert {
                cur-hop-limit 64
                link-mtu 0
                managed-flag false
                max-interval 600
                name-server 2a02:a47f:e000::53
                name-server 2a02:a47f:e000::54
                other-config-flag false
                prefix ::/64 {
                    autonomous-flag true
                    on-link-flag true
                    valid-lifetime 2592000
                }
                radvd-options "RDNSS 2a02:a47f:e000::53 2a02:a47f:e000::54 {};"
                reachable-time 0
                retrans-timer 0
                send-advert true
            }
        }
        speed auto
    }
    loopback lo {
    }
}
protocols {
    igmp-proxy {
        interface eth0.4 {
            alt-subnet 0.0.0.0/0
            role upstream
            threshold 1
        }
        interface eth3 {
            alt-subnet 0.0.0.0/0
            role downstream
            threshold 1
        }
    }
    static {
        interface-route6 ::/0 {
            next-hop-interface pppoe0 {
            }
        }
    }
}
service {
    dhcp-server {
        disabled false
        global-parameters "option vendor-class-identifier code 60 = string;"
        global-parameters "option broadcast-address code 28 = ip-address;"
        global-parameters "log-facility local2;"
        hostfile-update disable
        shared-network-name Thuis {
            authoritative enable
            subnet 192.168.2.0/24 {
                lease 86400
                start 192.168.2.1 {
                    stop 192.168.2.200
                }
            }
        }
        static-arp disable
        use-dnsmasq enable
    }
    dns {
        dynamic {
            interface pppoe0 {
                service namecheap {
                    host-name xxxx
                    login xxxx
                    password xxxx
                }
            }
        }
        forwarding {
            cache-size 4000
            listen-on eth3
            name-server 195.121.1.34
            name-server 195.121.1.66
            name-server 2a02:a47f:e000::53
            name-server 2a02:a47f:e000::54
            options listen-address=192.168.2.254
        }
    }
    gui {
        http-port 80
        https-port 443
        older-ciphers enable
    }
    nat {
        rule 5000 {
            description IPTV
            destination {
                address 213.75.112.0/21
            }
            log disable
            outbound-interface eth0.4
            protocol all
            source {
                address 192.168.2.0/24
            }
            type masquerade
        }
        rule 5001 {
            description IPTV
            destination {
                address 10.16.0.0/16
            }
            log disable
            outbound-interface eth0.4
            protocol all
            source {
                address 192.168.2.0/24
            }
            type masquerade
        }
        rule 5010 {
            description Internet
            log disable
            outbound-interface pppoe0
            protocol all
            type masquerade
        }
    }
    ssh {
        port 22
        protocol-version v2
    }
    telnet {
        port 23
    }
    unms {
        disable
    }
}
system {
    domain-name thuis.local
    host-name Thuis
    login {
        user ubnt {
            authentication {
                encrypted-password xxxx
                plaintext-password ""
            }
            full-name ""
            level admin
        }
    }
    name-server 127.0.0.1
    ntp {
        server 0.nl.pool.ntp.org {
        }
        server 1.nl.pool.ntp.org {
        }
        server ntp0.nl.net {
        }
        server ntp1.nl.net {
        }
        server time.kpn.net {
        }
    }
    offload {
        hwnat disable
        ipsec enable
        ipv4 {
            forwarding enable
            gre enable
            pppoe enable
            vlan enable
        }
        ipv6 {
            forwarding enable
            pppoe disable
            vlan enable
        }
    }
    package {
        repository stretch {
            components "main contrib non-free"
            distribution stretch
            password ""
            url http://http.us.debian.org/debian
            username ""
        }
    }
    static-host-mapping {
        host-name octopus {
            inet 192.168.2.201
        }
    }
    syslog {
        file dhcpd {
            archive {
                files 5
                size 5000
            }
            facility local2 {
                level debug
            }
        }
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
    }
    task-scheduler {
        task igmpcheck {
            executable {
                path /config/igmpcheck.sh
            }
            interval 5m
        }
        task igmprestart {
            executable {
                path /config/igmprestart.sh
            }
            interval 1d
        }
    }
    time-zone Europe/Amsterdam
    traffic-analysis {
        dpi disable
        export enable
    }
}
vpn {
    ipsec {
        allow-access-to-local-interface disable
        auto-firewall-nat-exclude disable
        ipsec-interfaces {
            interface eth0
        }
        nat-networks {
            allowed-network 192.168.0.0/16 {
            }
        }
        nat-traversal enable
    }
    l2tp {
        remote-access {
            authentication {
                local-users {
                    username xxxx {
                        password xxxx 
                    }
                    username xxxx {
                        password xxxx 
                    }
                }
                mode local
            }
            client-ip-pool {
                start 192.168.2.240
                stop 192.168.2.249
            }
            dns-servers {
                server-1 8.8.8.8
                server-2 8.8.4.4
            }
            idle 1800
            ipsec-settings {
                authentication {
                    mode pre-shared-secret
                    pre-shared-secret fiets
                }
                ike-lifetime 3600
                lifetime 3600
            }
            mtu 1350
            outside-address 0.0.0.0
        }
    }
}


/* Warning: Do not remove the following line. */
/* === vyatta-config-version: "config-management@1:conntrack@1:cron@1:dhcp-relay@1:dhcp-server@4:firewall@5:ipsec@5:nat@3:qos@1:quagga@2:suspend@1:system@4:ubnt-pptp@1:ubnt-udapi-server@1:ubnt-unms@1:ubnt-util@1:vrrp@1:vyatta-netflow@1:webgui@1:webproxy@1:zone-policy@1" === */
/* Release version: v2.0.8-hotfix.1.5278088.200305.1641 */
