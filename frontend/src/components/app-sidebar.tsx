import * as React from "react";
import {GalleryVerticalEnd} from "lucide-react";

import {
    Sidebar,
    SidebarContent, SidebarFooter,
    SidebarGroup,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarMenuSub,
    SidebarMenuSubButton,
    SidebarMenuSubItem, SidebarRail,
} from "@/components/ui/sidebar";
import {Link, useLocation} from "react-router";
import {auth} from "@/lib/auth.ts";
import {Button} from "@/components/ui/button.tsx";

const data = {
    navMain: [
        {
            url: "#",
            items: [
                {
                    title: "Dashboard",
                    url: "/dashboard",
                },
                {
                    title: "File Management",
                    url: "/dashboard/files",
                },
                {
                    title: "Key Management",
                    url: "/dashboard/keys/generate",
                },
                {
                    title: "Policy Builder",
                    url: "/dashboard/policies",
                },
                {
                    title: "File Encryption",
                    url: "/dashboard/files/encrypt",
                },
                {
                    title: "File Decryption",
                    url: "/dashboard/files/decrypt",
                },
            ],
        },
    ],
};


function handleLogout() {
    auth.logout();
}

export function AppSidebar({...props}: React.ComponentProps<typeof Sidebar>) {
    const location = useLocation();

    return (
        <Sidebar variant="inset" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton size="lg" asChild>
                            <a href="#">
                                <div
                                    className="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                                    <GalleryVerticalEnd className="size-4"/>
                                </div>
                                <div className="flex flex-col gap-0.5 leading-none">
                                    <span className="font-medium">Documentation</span>
                                    <span className="">v1.0.0</span>
                                </div>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent>
                <SidebarGroup>
                    <SidebarMenu className="gap-2">
                        {data.navMain.map((item) => (
                            <SidebarMenuItem>
                                {item.items?.length ? (
                                    <SidebarMenuSub className="ml-0 border-l-0 px-1.5">
                                        {item.items.map((subItem) => (
                                            <SidebarMenuSubItem key={subItem.title}>
                                                <SidebarMenuSubButton
                                                    asChild
                                                    isActive={location.pathname === subItem.url}
                                                >
                                                    <Link to={subItem.url}>{subItem.title}</Link>
                                                </SidebarMenuSubButton>
                                            </SidebarMenuSubItem>
                                        ))}
                                    </SidebarMenuSub>
                                ) : null}
                            </SidebarMenuItem>
                        ))}
                    </SidebarMenu>
                </SidebarGroup>
            </SidebarContent>
            <SidebarFooter>
                <Button variant={"default"} onClick={handleLogout}>Logout</Button>
            </SidebarFooter>
            <SidebarRail/>
        </Sidebar>
    );
}
